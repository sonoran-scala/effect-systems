scalaVersion := "2.13.6"

scalacOptions ++=
  Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-language:higherKinds",
    "-Ydelambdafy:inline"
  )

addCompilerPlugin(
  "org.typelevel" %% "kind-projector" % "0.13.0" cross CrossVersion.full
)

libraryDependencies += "org.typelevel" %% "cats-core" % "2.3.0"
libraryDependencies += "dev.zio" %% "zio" % "2.0.0-RC1"
