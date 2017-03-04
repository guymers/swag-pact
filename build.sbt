val projectName = "swag-pact"

lazy val buildSettings = Seq(
  name := projectName,
  organization := "swag-pact",
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
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  ),
  scalacOptions in Lint ++= Seq(
    "-Xlint",
    "-Ywarn-unused-import"
  ),

  wartremoverErrors := Seq.empty,
  scalacOptions in Compile := (scalacOptions in Compile).value filterNot { _ contains "wartremover" },

  conflictManager := ConflictManager.strict,
  dependencyOverrides += "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
) ++ lintSettings

// http://stackoverflow.com/a/27630768
lazy val Lint = config("lint") extend Compile

lazy val lintSettings = inConfig(Lint) {
  Defaults.compileSettings ++ Seq(
    sources := {
      (sources in Lint).value ++ (sources in Compile).value
    },
    wartremoverErrors := Warts.allBut()
  )
}

val catsVersion = "0.9.0"
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

      "org.typelevel" %% "cats-core" % catsVersion,

      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,

      "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
    ).map {
      _.excludeAll(
        ExclusionRule("org.slf4j", "slf4j-api"),
        ExclusionRule("com.fasterxml.jackson.core", "jackson-annotations"),
        ExclusionRule("com.fasterxml.jackson.core", "jackson-core"),
        ExclusionRule("com.fasterxml.jackson.core", "jackson-databind"),
        ExclusionRule("commons-io", "commons-io"),
        ExclusionRule("org.apache.commons", "commons-lang3")
      )
    },
    // excluded dependencies
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.21",
      "com.fasterxml.jackson.core" % "jackson-annotations" % "2.8.4",
      "com.fasterxml.jackson.core" % "jackson-core" % "2.8.4",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.4" exclude("com.fasterxml.jackson.core", "jackson-annotations"),
      "commons-io" % "commons-io" % "2.5",
      "org.apache.commons" % "commons-lang3" % "3.4"
    )
  )

lazy val scalatest = project.in(file("scalatest"))
  .dependsOn(core)
  .settings(buildSettings)
  .settings(moduleName := s"$projectName-scalatest")
  .settings(
    libraryDependencies ++= Seq(
      "au.com.dius" %% "pact-jvm-provider" % pactJvmVersion exclude("org.scalatest", "scalatest_2.11"),

      "org.scalatest" %% "scalatest" % scalaTestVersion,

      "com.github.tomakehurst" % "wiremock" % "2.5.1" % "test"
    ).map {
      _.excludeAll(
        ExclusionRule("org.slf4j", "slf4j-api"),
        ExclusionRule("com.fasterxml.jackson.core", "jackson-annotations"),
        ExclusionRule("com.fasterxml.jackson.core", "jackson-core"),
        ExclusionRule("com.fasterxml.jackson.core", "jackson-databind"),
        ExclusionRule("org.apache.httpcomponents", "httpclient")
      )
    },
    // excluded dependencies
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.21",
      "com.fasterxml.jackson.core" % "jackson-annotations" % "2.8.4" % "test",
      "com.fasterxml.jackson.core" % "jackson-core" % "2.8.4" % "test",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.4" % "test" exclude("com.fasterxml.jackson.core", "jackson-annotations"),
      "org.apache.httpcomponents" % "httpclient" % "4.5.2" % "test"
    )
  )
