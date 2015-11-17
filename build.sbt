name := "jwt4s"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.7"

organization := "software.sovereign"

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= {
  val scalazV = "7.1.5"

  Seq(
    "org.scalaz" %% "scalaz-core" % scalazV,
    "org.scalaz" %% "scalaz-concurrent" % scalazV,
    "org.specs2" %% "specs2-core" % "3.6.5" % "test",
    "io.jsonwebtoken" % "jjwt" % "0.6.0",
    "org.spire-math" %% "jawn-ast" % "0.8.3"
  )
}

scalacOptions in Test ++= Seq("-Yrangepos")

javaOptions ++= Seq(
  "-Xmx1G",
  "-Xms512M",
  "-Xmx2048M",
  "-Xss6M",
  "-XX:+CMSClassUnloadingEnabled",
  "-XX:+UseConcMarkSweepGC",
  //  "-XX:+UseG1GC",
  //  "-XX:MaxGCPauseMillis=200",
  //"-XX:+PrintGCDetails",
  "-XX:NewRatio=1"
)