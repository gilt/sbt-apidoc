package com.gilt.sbt.apidoc

import sbt._
import sbt.Keys._


object ApidocPlugin extends AutoPlugin {

  object autoImport {
    val apidocUrl = settingKey[String]("base URL for apidoc (default http://api.apidoc.me)")
    val apidocSource = settingKey[File]("location of the api file (default ./api.json)")
    val apidocServiceName = settingKey[String]("apidoc service name")
    val apidocOrganization = settingKey[String]("apidoc organization")
    val apidocVisibility = settingKey[String]("visibility in apidoc ('organization' or 'public'")
    val apidocValidate = taskKey[Boolean]("validates api.json")
    val apidocPublish = taskKey[Unit]("Publishes api.json")
  }

  import autoImport._

  override def trigger = allRequirements

  override lazy val projectSettings = Seq(
    apidocUrl := "http://api.apidoc.me",
    apidocSource := baseDirectory.value / "api.json",
    apidocServiceName := name.value,
    apidocVisibility := "organization",
    apidocValidate := Apidoc.validate(apidocUrl.value, apidocSource.value, streams.value),
    apidocPublish := Apidoc.publish(apidocUrl.value, apidocSource.value, apidocOrganization.value, apidocServiceName.value, version.value, apidocVisibility.value, streams.value)
  )

}


object Apidoc {
  import scala.concurrent.Await
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  import scala.language.postfixOps

  import com.gilt.apidoc.Client
  import com.gilt.apidoc.models.{Version, Visibility}

  def validate(apidocUrl: String, apidocSource: File, s: TaskStreams): Boolean = {

    val validations = new Client(apidocUrl).validations

    val apijson = IO.read(apidocSource)

    s.log.debug(s"validating api.json: $apijson")

    val validation = Await.result(validations.post(apijson), 30 seconds)

    if (!validation.valid) {
      s.log.error("api.json validation failed")
      validation.errors.foreach { s.log.error(_) }
    }

    validation.valid
  }

  def publish(apidocUrl: String, apidocSource: File, apidocOrg: String, serviceName: String, version: String, visibility: String, s: TaskStreams): Unit = {

    val versions = new Client(apidocUrl).versions

    val apijson = IO.read(apidocSource)

    Await.result(
      versions.putByOrgKeyAndServiceKeyAndVersion(
        apidocOrg,
        serviceName,
        version,
        apijson,
        Visibility.fromString(visibility)
      ),
      30 seconds
    )
  }
}
