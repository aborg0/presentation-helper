package com.github.aborg0.presentation_helper.config

import com.github.aborg0.presentation_helper.config.AppConfig._

case class AppConfig(repository: String,
                     knownBranches: Seq[Branch],
                     style: String,
                     transparency: Double = .7,
                     left: Int = 1500,
                     top: Int = 130,
                     width: Int = 410,
                     height: Int = 870,
) {
  def repositoryPath =
    if (repository.endsWith(".git")) repository else repository + "/.git"
  def stylePath = if (style.endsWith(".css")) style else style + ".css"
}

object AppConfig {

  case class Branch(name: String,
                    displayName: Option[String],
                    description: Option[String],
                    state: BranchState = BranchState.Visible)

  sealed trait BranchState

  object BranchState {
    case object Hidden  extends BranchState
    case object Visible extends BranchState
  }

//  object BranchStates extends Enumeration {
//    type BranchState = Value
//    val Hidden, Visible = Value
//  }

}
