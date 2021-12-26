package tf

object Main extends App {

  trait Monad[M[_]] {
    def pure[A](x: A): M[A]
    def map[A, B](x: M[A])(f: A => B): M[B] =
      flatMap(x)(a => pure(f(a)))
    def flatMap[A, B](x: M[A])(f: A => M[B]): M[B]
  }

  implicit class MonadOps[M[_]: Monad, A](ma: M[A]) {
    val M = implicitly[Monad[M]]
    def map[B](f: A => B): M[B] = M.map(ma)(f)
    def flatMap[B](f: A => M[B]): M[B] = M.flatMap(ma)(f)
  }

  type ID[A] = A

  implicit val idMonad: Monad[ID] =
    new Monad[ID] {
      override def pure[A](x: A): ID[A] = x
      override def flatMap[A, B](x: ID[A])(f: A => ID[B]): ID[B] = f(x)
    }

  trait Algebra[F[_]] {
    def readEnv(name: String): F[String]
    def readLn: F[String]
    def write(output: String): F[Unit]
  }

  class Program[F[_]: Monad](x: Algebra[F]) {

    def enProgram(): F[Unit] =
      for {
        _ <- x.write("What's your name? ")
        name <- x.readLn
        _ <- x.write(s"Hello, ${name}!\n")
      } yield ()

    def esProgram(): F[Unit] =
      for {
        _ <- x.write("¿Cómo te llamas? ")
        name <- x.readLn
        _ <- x.write(s"¡Hola, ${name}!\n")
      } yield ()

    def program: F[Unit] =
      for {
        lang <- x.readEnv("LANG")
        _ <-
          if (lang.startsWith("es")) {
            esProgram()
          } else {
            enProgram()
          }
      } yield ()
  }

  object Interpreter extends Algebra[ID] {
    override def readEnv(name: String): ID[String] = sys.env(name)
    override def readLn: ID[String] = scala.io.StdIn.readLine()
    override def write(output: String): ID[Unit] = print(output)
  }

  new Program(Interpreter).program
}

object Cats extends App {

  trait Algebra[F[_]] {
    def readEnv(name: String): F[String]
    def readLn: F[String]
    def write(output: String): F[Unit]
  }

  class Program[F[_]: cats.Monad](x: Algebra[F]) {

    // `flatMap` extension method
    import cats.implicits.toFlatMapOps

    // `map` extension method
    import cats.implicits.toFunctorOps

    def enProgram(): F[Unit] =
      for {
        _ <- x.write("What's your name? ")
        name <- x.readLn
        _ <- x.write(s"Hello, ${name}!\n")
      } yield ()

    def esProgram(): F[Unit] =
      for {
        _ <- x.write("¿Cómo te llamas? ")
        name <- x.readLn
        _ <- x.write(s"¡Hola, ${name}!\n")
      } yield ()

    def program: F[Unit] =
      for {
        lang <- x.readEnv("LANG")
        _ <-
          if (lang.startsWith("es")) {
            esProgram()
          } else {
            enProgram()
          }
      } yield ()
  }

  object Interpreter extends Algebra[cats.Id] {
    override def readEnv(name: String): cats.Id[String] = sys.env(name)
    override def readLn: cats.Id[String] = scala.io.StdIn.readLine()
    override def write(output: String): cats.Id[Unit] = print(output)
  }

  new Program(Interpreter).program
}
