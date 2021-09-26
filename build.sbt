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
