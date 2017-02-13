val projectName = "swag-pact"

lazy val buildSettings = Seq(
  name := projectName,
  scalaVersion := "2.11.8",

  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Ypatmat-exhaust-depth", "off",
    "-Xfatal-warnings",
    "-Xfuture",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused-import",
    "-Ywarn-value-discard"
  )

//  conflictManager := ConflictManager.strict
)

val circeVersion = "0.7.0"
val pactJvmVersion = "3.3.6"
val scalaTestVersion = "3.0.1"

lazy val root = project.in(file("."))
  .settings(buildSettings)
  .aggregate(core, scalatest)

lazy val core = project.in(file("core"))
  .settings(buildSettings)
  .settings(moduleName := s"$projectName-core")
  .settings(
    libraryDependencies ++= Seq(
      "io.swagger" % "swagger-parser" % "1.0.25",
      "au.com.dius" % "pact-jvm-model" % pactJvmVersion,
      "org.apache.httpcomponents" % "httpcore" % "4.4.4",

      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,

      "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
    ).map {
      _.excludeAll(
        ExclusionRule("org.slf4j", "slf4j-api"),
        ExclusionRule("com.fasterxml.jackson.core", "jackson-annotations"),
        ExclusionRule("com.fasterxml.jackson.core", "jackson-core"),
        ExclusionRule("com.fasterxml.jackson.core", "jackson-databind")
      )
    },
    // excluded dependencies
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.21",
      "com.fasterxml.jackson.core" % "jackson-annotations" % "2.8.4",
      "com.fasterxml.jackson.core" % "jackson-core" % "2.8.4",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.4" exclude("com.fasterxml.jackson.core", "jackson-annotations")
    )
  )

lazy val scalatest = project.in(file("scalatest"))
  .dependsOn(core)
  .settings(buildSettings)
  .settings(moduleName := s"$projectName-scalatest")
  .settings(
    libraryDependencies ++= Seq(
      "au.com.dius" %% "pact-jvm-provider" % pactJvmVersion,

      "org.scalatest" %% "scalatest" % scalaTestVersion
    )
  )
