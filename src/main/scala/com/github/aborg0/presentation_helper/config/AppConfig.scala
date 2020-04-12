package com.github.aborg0.presentation_helper.config

import com.github.aborg0.presentation_helper.config.AppConfig._

case class AppConfig(repository: String, knownBranches: Seq[Branch]) {
  def repositoryPath = if (repository.endsWith(".git")) repository else repository + "/.git"
}

object AppConfig {

  case class Branch(name: String,
                    description: Option[String],
                    state: BranchState = BranchState.Visible)

  sealed trait BranchState

  object BranchState {
    case object Hidden extends BranchState
    case object Visible extends BranchState
  }

//  object BranchStates extends Enumeration {
//    type BranchState = Value
//    val Hidden, Visible = Value
//  }

}
