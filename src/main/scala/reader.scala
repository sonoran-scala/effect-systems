package reader

object Main extends App {

  case class Reader[-E, +A](run: E => A) {

    def map[B](f: A => B): Reader[E, B] =
      Reader(e => f(run(e)))

    def flatMap[E1 <: E, B](f: A => Reader[E1, B]): Reader[E1, B] =
      Reader(e => f(run(e)).run(e))
  }

  trait HasEnv {
    def env: Map[String, String]
  }

  def readEnv[E <: HasEnv](name: String): Reader[E, String] =
    Reader(r => r.env(name))

  trait HasReadLn {
    def readLn(): String
  }

  def readLn[E <: HasReadLn](): Reader[E, String] =
    Reader(r => r.readLn())

  trait HasWrite {
    def write(output: String): Unit
  }

  def write[E <: HasWrite](output: String): Reader[E, Unit] =
    Reader(r => r.write(output))

  val enProgram: Reader[HasReadLn with HasWrite, Unit] =
    for {
      _ <- write("What's your name? ")
      name <- readLn()
      _ <- write(s"Hello, ${name}!\n")
    } yield ()

  val esProgram: Reader[HasReadLn with HasWrite, Unit] =
    for {
      _ <- write("¿Cómo te llamas? ")
      name <- readLn()
      _ <- write(s"¡Hola, ${name}!\n")
    } yield ()

  val program: Reader[HasEnv with HasReadLn with HasWrite, Unit] =
    for {
      lang <- readEnv[HasEnv with HasReadLn with HasWrite]("LANG")
      _ <-
        if (lang.startsWith("es")) {
          esProgram
        } else {
          enProgram
        }
    } yield ()

  program.run {
    new HasEnv with HasReadLn with HasWrite {
      override val env: Map[String, String] = sys.env
      override def readLn(): String = scala.io.StdIn.readLine()
      override def write(output: String): Unit = print(output)
    }
  }
}

object ZIO extends App {

  trait HasEnv {
    def env: Map[String, String]
  }

  def readEnv(name: String): zio.ZIO[HasEnv, Throwable, String] =
    zio.ZIO.environmentWithZIO { r =>
      zio.ZIO.attempt {
        r.get.env(name)
      }
    }

  trait HasReadLn {
    def readLn(): String
  }

  val readLn: zio.ZIO[HasReadLn, Throwable, String] =
    zio.ZIO.environmentWithZIO { r =>
      zio.ZIO.attempt {
        r.get.readLn()
      }
    }

  trait HasWrite {
    def write(output: String): Unit
  }

  def write(output: String): zio.ZIO[HasWrite, Throwable, Unit] =
    zio.ZIO.environmentWithZIO { r =>
      zio.ZIO.attempt {
        r.get.write(output)
      }
    }

  val enProgram: zio.ZIO[HasReadLn with HasWrite, Throwable, Unit] =
    for {
      _ <- write("What's your name? ")
      name <- readLn
      _ <- write(s"Hello, ${name}!\n")
    } yield ()

  val esProgram: zio.ZIO[HasReadLn with HasWrite, Throwable, Unit] =
    for {
      _ <- write("¿Cómo te llamas? ")
      name <- readLn
      _ <- write(s"¡Hola, ${name}!\n")
    } yield ()

  val program
      : zio.ZIO[HasEnv with HasReadLn with HasWrite, Throwable, Unit] =
    for {
      lang <- readEnv("LANG")
      _ <-
        if (lang.startsWith("es")) {
          esProgram
        } else {
          enProgram
        }
    } yield ()

  zio.Runtime.default.unsafeRun {
    program.provideLayer {
      zio.ZLayer.succeed {
        new HasEnv {
          override val env: Map[String, String] = sys.env
        }
      } ++ zio.ZLayer.succeed {
        new HasReadLn {
          override def readLn(): String = scala.io.StdIn.readLine()
        }
      } ++ zio.ZLayer.succeed {
        new HasWrite {
          override def write(output: String): Unit = print(output)
        }
      }
    }
  }
}
