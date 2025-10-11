scalaVersion := "2.13.17"

addCompilerPlugin(
  "org.typelevel" %% "kind-projector" % "0.13.4" cross CrossVersion.full
)

libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0"
libraryDependencies += "dev.zio" %% "zio" % "2.1.21"
