organization := "com.gilt.sbt"

name := "sbt-apidoc"

version := "git describe --tags --dirty --always".!!.trim

sbtPlugin := true

scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-feature",
  "-encoding", "UTF-8"
)

javaVersionPrefix in javaVersionCheck := Some("1.6")

ScriptedPlugin.scriptedSettings

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value)
}

scriptedBufferLog := false

scalariformSettings

publishMavenStyle := false

bintraySettings

bintrayPublishSettings

bintray.Keys.bintrayOrganization in bintray.Keys.bintray := Some("giltgroupe")

bintray.Keys.packageLabels in bintray.Keys.bintray := Seq("sbt", "apidoc")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
