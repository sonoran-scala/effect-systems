scalaVersion := "2.13.12"

addCompilerPlugin(
  "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
)

libraryDependencies += "org.typelevel" %% "cats-core" % "2.10.0"
libraryDependencies += "dev.zio" %% "zio" % "2.0.16"
