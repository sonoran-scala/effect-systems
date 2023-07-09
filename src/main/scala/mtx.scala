package mtx

object Main extends App {

  trait Monad[M[_]] {
    def pure[A](x: A): M[A]
    def flatMap[A, B](x: M[A])(f: A => M[B]): M[B]
    def map[A, B](x: M[A])(f: A => B): M[B] =
      flatMap(x)(a => pure(f(a)))
  }

  implicit class MonadOps[M[_]: Monad, A](ma: M[A]) {
    val M = implicitly[Monad[M]]
    def flatMap[B](f: A => M[B]): M[B] = M.flatMap(ma)(f)
    def map[B](f: A => B): M[B] = M.map(ma)(f)
  }

  type ID[A] = A

  implicit val idMonad: Monad[ID] =
    new Monad[ID] {
      override def pure[A](x: A): ID[A] = x
      override def flatMap[A, B](x: ID[A])(f: A => ID[B]): ID[B] = f(x)
    }

  abstract class ReadEnvT[F[_], A] {
    def runEnv(env: Map[String, String]): F[A]
  }

  implicit def readEnvTMonad[F[_]: Monad]: Monad[ReadEnvT[F, *]] =
    new Monad[ReadEnvT[F, *]] {

      val F = implicitly[Monad[F]]

      override def pure[A](x: A): ReadEnvT[F, A] =
        new ReadEnvT[F, A] {
          override def runEnv(env: Map[String, String]): F[A] =
            F.pure(x)
        }

      override def flatMap[A, B](
          x: ReadEnvT[F, A]
      )(f: A => ReadEnvT[F, B]): ReadEnvT[F, B] =
        new ReadEnvT[F, B] {
          override def runEnv(env: Map[String, String]): F[B] =
            F.flatMap(F.map(x.runEnv(env))(f))(_.runEnv(env))
        }
    }

  abstract class ReadLnT[F[_], A] {
    def runIn(readLn: () => String): F[A]
  }

  implicit def readLnTMonad[F[_]: Monad]: Monad[ReadLnT[F, *]] =
    new Monad[ReadLnT[F, *]] {

      val F = implicitly[Monad[F]]

      override def pure[A](x: A): ReadLnT[F, A] =
        new ReadLnT[F, A] {
          override def runIn(readLn: () => String): F[A] =
            F.pure(x)
        }

      override def flatMap[A, B](
          x: ReadLnT[F, A]
      )(f: A => ReadLnT[F, B]): ReadLnT[F, B] =
        new ReadLnT[F, B] {
          override def runIn(readLn: () => String): F[B] =
            F.flatMap(F.map(x.runIn(readLn))(f))(_.runIn(readLn))
        }
    }

  abstract class WriteT[F[_], A] {
    def runOut(write: String => Unit): F[A]
  }

  implicit def writeTMonad[F[_]: Monad]: Monad[WriteT[F, *]] =
    new Monad[WriteT[F, *]] {

      val F = implicitly[Monad[F]]

      override def pure[A](x: A): WriteT[F, A] =
        new WriteT[F, A] {
          override def runOut(write: String => Unit): F[A] =
            F.pure(x)
        }

      override def flatMap[A, B](
          x: WriteT[F, A]
      )(f: A => WriteT[F, B]): WriteT[F, B] =
        new WriteT[F, B] {
          override def runOut(write: String => Unit): F[B] =
            F.flatMap(F.map(x.runOut(write))(f))(_.runOut(write))
        }
    }

  type Program[A] =
    WriteT[ReadEnvT[ReadLnT[ID[*], *], *], A]

  def readEnv(name: String): Program[String] =
    new WriteT[ReadEnvT[ReadLnT[ID[*], *], *], String] {
      def runOut(
          write: String => Unit
      ): ReadEnvT[ReadLnT[ID[*], *], String] = {
        new ReadEnvT[ReadLnT[ID[*], *], String] {
          override def runEnv(
              env: Map[String, String]
          ): ReadLnT[ID, String] =
            new ReadLnT[ID, String] {
              override def runIn(readLn: () => String): String = {
                env(name)
              }
            }
        }
      }
    }

  def readLn(): Program[String] =
    new WriteT[ReadEnvT[ReadLnT[ID[*], *], *], String] {
      def runOut(
          write: String => Unit
      ): ReadEnvT[ReadLnT[ID[*], *], String] = {
        new ReadEnvT[ReadLnT[ID[*], *], String] {
          override def runEnv(
              env: Map[String, String]
          ): ReadLnT[ID, String] =
            new ReadLnT[ID, String] {
              override def runIn(readLn: () => String): String = {
                readLn()
              }
            }
        }
      }
    }

  def write(output: String): Program[Unit] =
    new WriteT[ReadEnvT[ReadLnT[ID[*], *], *], Unit] {
      def runOut(
          write: String => Unit
      ): ReadEnvT[ReadLnT[ID[*], *], Unit] = {
        new ReadEnvT[ReadLnT[ID[*], *], Unit] {
          override def runEnv(
              env: Map[String, String]
          ): ReadLnT[ID, Unit] =
            new ReadLnT[ID, Unit] {
              override def runIn(readLn: () => String): Unit = {
                write(output)
              }
            }
        }
      }
    }

  val enProgram: Program[Unit] =
    for {
      _ <- write("What's your name? ")
      name <- readLn()
      _ <- write(s"Hello, ${name}!\n")
    } yield ()

  val esProgram: Program[Unit] =
    for {
      _ <- write("¿Cómo te llamas? ")
      name <- readLn()
      _ <- write(s"¡Hola, ${name}!\n")
    } yield ()

  val program: Program[Unit] =
    for {
      lang <- readEnv("LANG")
      _ <-
        if (lang.startsWith("es")) {
          esProgram
        } else {
          enProgram
        }
    } yield ()

  program
    .runOut(print)
    .runEnv(sys.env)
    .runIn(scala.io.StdIn.readLine)
}
