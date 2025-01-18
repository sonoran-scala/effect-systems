scalaVersion := "2.13.16"

addCompilerPlugin(
  "org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full
)

libraryDependencies += "org.typelevel" %% "cats-core" % "2.12.0"
libraryDependencies += "dev.zio" %% "zio" % "2.1.14"
