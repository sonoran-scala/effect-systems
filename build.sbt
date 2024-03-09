scalaVersion := "2.13.13"

addCompilerPlugin(
  "org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full
)

libraryDependencies += "org.typelevel" %% "cats-core" % "2.10.0"
libraryDependencies += "dev.zio" %% "zio" % "2.0.21"
