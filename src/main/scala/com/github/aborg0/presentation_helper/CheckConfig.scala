package com.github.aborg0.presentation_helper

import java.io.File

import com.github.aborg0.presentation_helper.config.AppConfig
import org.eclipse.jgit.api.Git
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.jdk.CollectionConverters._

object CheckConfig extends App {
  val config = ConfigSource.default.loadOrThrow[AppConfig]

  import org.eclipse.jgit.storage.file.FileRepositoryBuilder

  val builder = new FileRepositoryBuilder
  val repository = builder.setGitDir(new File(config.repositoryPath)).readEnvironment.findGitDir // scan environment GIT_* variables
    .build // scan up the file system tree

  val repoBranches = new Git(repository).branchList().call.asScala.map(_.getName).to(Set)

  val missingBranches = config.knownBranches.filterNot(branch =>
    repoBranches.contains("refs/heads/" + branch.name))
  require(missingBranches.isEmpty, "The following branches are configured, but not present in the repository" +
    missingBranches.mkString(", ") + s" the repository branches are: ${repoBranches.toSeq.sorted}")
}
