scalaVersion := "2.13.7"
name := "db"

lazy val scalaTestVersion = "3.2.10"
val testDeps = Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion,
  "org.scalatest" %% "scalatest-flatspec" % scalaTestVersion,
  "org.scalatestplus" %% "scalacheck-1-15" % (scalaTestVersion + ".0")
).map(_ % Test)

libraryDependencies ++= testDeps
