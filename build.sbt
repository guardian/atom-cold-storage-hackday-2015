organization  := "com.gu"

name          := "atom-cold-storage"

version       := "0.1"

scalaVersion  := "2.11.7"

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "org.slf4j"         %   "slf4j-simple" % "1.7.13",
    "com.amazonaws"     %   "amazon-kinesis-client" % "1.6.1",
    "com.gu"            %%  "content-atom-model-scala" % "0.2.1-SNAPSHOT",
    "io.spray"          %%  "spray-can"     % sprayV,
    "io.spray"          %%  "spray-routing" % sprayV,
    "io.spray"          %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka" %%  "akka-actor"    % akkaV,
    "com.typesafe.akka" %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"        %%  "specs2-core"   % "2.3.11" % "test"
  )
}

Revolver.settings
