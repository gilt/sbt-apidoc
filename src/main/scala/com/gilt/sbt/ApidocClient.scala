package com.gilt.sbt.apidoc.client

import sbt._
import sbt.Keys._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

object ApidocClient extends AutoPlugin {

  object autoImport {
    val apidocClientDependencies = settingKey[Seq[ModuleID]]("apidoc client services to download")
    val apidocClientCode = taskKey[Seq[File]]("Download clients from apidoc code generator")
    val apidocServer = settingKey[URI]("apidoc server from which to obtain generated code")
  }

  import autoImport._

  override val requires = plugins.JvmPlugin

  override def projectSettings: Seq[Setting[_]] = Seq(
    apidocClientDependencies := Seq(),
    apidocServer := uri("http://www.apidoc.me/"),
    apidocClientCode := getAllCode(sourceManaged.value / "apidoc_clients", apidocClientDependencies.value, streams.value.log),
    sourceGenerators in Compile += apidocClientCode.taskValue
  )

  // Return the filename used for a particular service
  private[client] def fileNameForService(s: ModuleID): String =
    s"${s.organization}.${s.name}.${s.revision}.${s.configurations.get}".replaceAll("[-.]", "_") + ".scala"

  // Return org/service combinations which are specified multiple times in apidocServices
  private[client] def duplicateOrgAndService(services: Seq[ModuleID]): Seq[(String, String)] = {
    services.groupBy(s ⇒ (s.organization, s.name)).filter(_._2.size > 1).map(_._1).toSeq
  }

  // For a particular service, return the File representing its implementation
  private[client] def getTargetFile(targetDirectory: File, s: ModuleID): File = {
    targetDirectory / fileNameForService(s)
  }

  // Write code for service into file in target directory
  private[client] def writeFile(targetFile: File, s: ModuleID, log: sbt.Logger)(code: String): Unit = {
    log.info(s"Writing apidoc client to ${targetFile.getAbsolutePath}")
    IO.write(targetFile, code)
  }

  // Get code for all services, save into target directory
  private[client] def getAllCode(targetDirectory: File, services: Seq[ModuleID], log: sbt.Logger): Seq[File] = {
    val duplicates = duplicateOrgAndService(services)

    if (duplicates.nonEmpty)
      sys.error(s"Duplicate org and service: ${duplicates.mkString}")

    val filesAndServices = services.map { s ⇒ (s, getTargetFile(targetDirectory, s)) }

    val requiredFiles = filesAndServices.map(_._2).toSet

    val futureWrites = filesAndServices filterNot (_._2.isFile) map { s ⇒
      getCode(s._1) map (writeFile(s._2, s._1, log) _)
    }

    val filesToDelete = (targetDirectory.***).filter(f ⇒ !requiredFiles.contains(f) && f != targetDirectory).get

    filesToDelete foreach { f ⇒
      log.info(s"Deleting unused apidoc file ${f.getAbsolutePath}")
      f.delete()
    }

    Await.result(Future.sequence(futureWrites), 60.seconds)

    filesAndServices.map(_._2)
  }

  // Get code implementation for the client of the specified service
  private[client] def getCode(service: ModuleID): Future[String] = {
    Future.successful(
      s"""package ${service.organization}.apidoc_client
        |
        |object Version {
        |  val version = "${service.revision}"
        |}
      """.stripMargin)
  }
}
