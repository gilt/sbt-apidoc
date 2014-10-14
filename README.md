# Apidoc SBT Plugin

This SBT plugin automates interactions with apidoc, and is designed to integrate into your release process.

## Configuration

In `project/build.sbt` or `project/plugins.sbt`,

```scala
addSbtPlugin("com.gilt.sbt" % "sbt-apidoc" % "0.0.1-SNAPSHOT")
```

In `build.sbt`,

```scala
apidocOrganization := "initech"

apidocServiceName := "flare"          // defaults to 'name'

apidocVisibility := "organization"    // or "public"
```

By default, the plugin expects `api.json` to be found in the base directory, but you can specify another location with `apidocSource`.  If you maintain a private Apidoc server, you can also set the `apidocUrl` key.

## Usage

To validate your api.json, run `sbt apidocValidate`.

To publish your api.json, run `sbt apidocPublish`.

If you use the `sbt-release` plugin, it would be recommended to incorporate these as the check and action in a ReleaseStep.
