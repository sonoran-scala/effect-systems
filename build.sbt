scalaVersion := "2.13.11"

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
  "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
)

libraryDependencies += "org.typelevel" %% "cats-core" % "2.9.0"
libraryDependencies += "dev.zio" %% "zio" % "2.0.15"
