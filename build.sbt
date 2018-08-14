//import AssemblyKeys._

//assemblySettings

scalaVersion := "2.12.4"

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case PathList(ps @ _*) if ps.head startsWith "scala" => MergeStrategy.last
 case x => MergeStrategy.first
}

scalacOptions := Seq("-unchecked", "-deprecation", "-feature")

assemblyJarName in assembly := "vArmyKnife.jar"

mainClass in assembly := Some("runner.runner")

