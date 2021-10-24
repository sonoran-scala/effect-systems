package free

object Main extends App {

  trait ~>[F[_], G[_]] {
    def apply[A](f: F[A]): G[A]
  }

  trait Monad[M[_]] {
    def pure[A](x: A): M[A]
    def map[A, B](x: M[A])(f: A => B): M[B] = flatMap(x)(a => pure(f(a)))
    def flatMap[A, B](x: M[A])(f: A => M[B]): M[B]
  }

  type ID[A] = A

  implicit val idMonad: Monad[ID] =
    new Monad[ID] {
      override def pure[A](x: A): ID[A] = x
      override def flatMap[A, B](x: ID[A])(f: A => ID[B]): ID[B] = f(x)
    }

  trait Free[F[_], A] {

    import Free._

    def map[B](f: A => B): Free[F, B] = flatMap { a => pure(f(a)) }

    def flatMap[B](f: A => Free[F, B]): Free[F, B] = Bind(this, f)

    def foldMap[G[_]: Monad](nt: F ~> G): G[A] = this match {
      case Pure(a) =>
        implicitly[Monad[G]].pure(a)
      case Suspend(fa) =>
        nt(fa)
      case Bind(fa, f) =>
        val mg = implicitly[Monad[G]]
        val ga = fa.foldMap(nt)
        mg.flatMap(ga)(f(_).foldMap(nt))
    }
  }

  case class Pure[F[_], A](a: A) extends Free[F, A]
  case class Suspend[F[_], A](fa: F[A]) extends Free[F, A]
  case class Bind[F[_], A, B](fa: Free[F, A], f: A => Free[F, B])
      extends Free[F, B]

  object Free {

    def pure[F[_], A](a: A): Free[F, A] = Pure(a)

    def liftM[F[_], A](fa: F[A]): Free[F, A] = Suspend(fa)
  }

  trait Program[A]
  case class ReadEnv(name: String) extends Program[String]
  case object ReadLn extends Program[String]
  case class Write(output: String) extends Program[Unit]

  val enProgram: Free[Program, Unit] =
    for {
      _ <- Free.liftM(Write("What's your name? "))
      name <- Free.liftM(ReadLn)
      _ <- Free.liftM(Write(s"Hello, ${name}!\n"))
    } yield ()

  val esProgram: Free[Program, Unit] =
    for {
      _ <- Free.liftM(Write("¿Cómo te llamas? "))
      name <- Free.liftM(ReadLn)
      _ <- Free.liftM(Write(s"¡Hola, ${name}!\n"))
    } yield ()

  val program: Free[Program, Unit] =
    for {
      lang <- Free.liftM(ReadEnv("LANG"))
      _ <-
        if (lang.startsWith("es")) {
          esProgram
        } else {
          enProgram
        }
    } yield ()

  program.foldMap[ID] {
    new ~>[Program, ID] {
      def apply[A](x: Program[A]): ID[A] =
        x match {
          case ReadEnv(name) => sys.env(name)
          case ReadLn        => scala.io.StdIn.readLine()
          case Write(output) => print(output)
        }
    }
  }
}
