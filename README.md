This is the code for a series of events about different approaches to
[effect systems][1] in Scala.

For each event in the series, we:

1. Describe an effectful program that we would like to write
2. Discuss background and practical details about an effect system
3. Write a toy implementation of the effect system
4. Build and run the effectful program within the effect system

We express the following as effects:

* Read text from stdin
* Read environment variables
* Write text to stdout

Our effectful programs look something like this:

```scala
val enProgram =
  for {
    _    <- write("What's your name? ")
    name <- readLn()
    _    <- write(s"Hello, ${name}!\n")
  } yield ()

val esProgram =
  for {
    _    <- write("¿Cómo te llamas? ")
    name <- readLn()
    _    <- write(s"¡Hola, ${name}!\n")
  } yield ()

val program =
  for {
    lang <- readEnv("LANG")
    _    <- if (lang.startsWith("es")) {
              esProgram
            } else {
              enProgram
            }
  } yield ()
```

[1]: https://en.wikipedia.org/wiki/Effect_system
