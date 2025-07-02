ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .settings(
    name := "formacionSpark",
    idePackagePrefix := Some("com.curso")
  )

// Dependencias
// Meteremos las dependencias de Spark
// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-parallel-collections
libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0"