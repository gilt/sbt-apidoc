Introduction
------------

ApiDoc is a service for describing a REST interface, and can generate
clients/server routing that implements the interface. 

sbt-apidoc-client provides SBT with the ability to download specific client
code, and makes it available to projects.

Requirements
------------
- SBT 0.13.5+

Usage
-----

To include sbt-apidoc-client in your build, add this to `plugins.sbt`:

```scala
addSbtPlugin("com.gilt.sbt" % "sbt-apidoc-client" % "0.0.1")
```

To enable Apidoc client downloads for a project, call the `enablePlugins` method on the project. e.g.

```scala
lazy val myProject = project.in(file(".")).enablePlugins(ApidocClient)
```

In cases where you have only one root build, you can call it directly from `build.sbt` with:

```scala
enablePlugins(ApidocClient)
```

To download a particular client, modify the `apidocClientDependencies` setting key for the project:

```scala
apidocClientDependencies += "org" % "service" % "version" % "client_type"
```

As usual, to add multiple dependencies, you may add a sequence at once:

```scala
apidocClientDependencies ++= Seq(
  "org" % "service" % "version" % "client_type",
  "org2" % "service2" % "version2" % "client_type2"
)
```

Notes
-----
1. The user of the plugin is responsible for adding any extra libraries that the code depends on. For example, clients may require a particular JSON or HTTP request library.
2. Implementations are placed under `(managedSource in Compile).value / apidoc_clients`
3. Files are never refreshed after downloading. So if a particular version of an interface changes, this can only be detected by cleaning the project (e.g. `sbt clean`). It is recommended to generally not change APIs after publishing them. This is also useful to provide repeatable builds.
4. You may only specify a single version of a service (unique organization and service name).
