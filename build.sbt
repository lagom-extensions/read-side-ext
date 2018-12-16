name := "lagom-read-side-ext"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  lagomScaladslPersistenceJdbc % "provided",
  lagomScaladslPersistenceCassandra % "provided"
)

organization := "com.github.lagom-extensions"
homepage := Some(url("https://github.com/lagom-extensions/read-side-ext"))
scmInfo := Some(ScmInfo(url("https://github.com/lagom-extensions/read-side-ext"), "git@github.com:lagom-extensions/read-side-ext.git"))
developers := List(Developer("kuzkdmy", "Dmitriy Kuzkin", "mail@dmitriy.kuzkin@gmail.com", url("https://github.com/kuzkdmy")))
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
publishMavenStyle := true

publishTo := Some(
  if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
  else Opts.resolver.sonatypeStaging
)
