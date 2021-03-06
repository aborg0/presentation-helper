package com.github.aborg0.presentation_helper

import java.io.File
import java.nio.file._

import com.github.aborg0.presentation_helper.config.AppConfig
import com.github.aborg0.presentation_helper.config.AppConfig.BranchState
import javafx.scene.input
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import scalafx.application.{JFXApp, Platform}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.binding.Bindings
import scalafx.beans.binding.Bindings._
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane
import scalafx.scene.layout.VBox
import scalafx.scene.text.Text
import scalafx.stage.StageStyle

import scala.jdk.CollectionConverters._

object App extends JFXApp {
  val config = (parameters.unnamed match {
    case Seq()         => ConfigSource.default
    case _ => ConfigSource.file(path = parameters.unnamed.head)
  }).loadOrThrow[AppConfig]

  val watchService = FileSystems.getDefault.newWatchService
  val gitPath      = Paths.get(config.repositoryPath)
  val builder      = new FileRepositoryBuilder
  val repository = builder
    .setGitDir(new File(config.repositoryPath))
    .readEnvironment
    .findGitDir // scan environment GIT_* variables
    .build // scan up the file system tree
  var watchKey: WatchKey = null
  gitPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE)
  private val thread = new Thread(
    () => {
      while ({
        watchKey = watchService.take();
        watchKey != null
      }) {
        for (event <- watchKey.pollEvents().asScala
             if event.context().toString == "HEAD") {
          val branchName = repository.getBranch
          Platform.runLater {
            branchNameProperty.value = branchName
            branchNameProperty()
          }
        }
        watchKey.reset()
      }
    },
    "file watcher thread"
  )
  thread.setDaemon(true)
  thread.start()

  val branchNameProperty = new StringProperty(repository.getBranch)
  val descriptionProperty = Bindings.createStringBinding(
    () =>
      config.knownBranches
        .find(_.name == repository.getBranch)
        .flatMap(_.description)
        .getOrElse(""),
    branchNameProperty)
  val nameProperty = Bindings.createStringBinding(
    () =>
      config.knownBranches
        .find(_.name == repository.getBranch)
        .flatMap(_.displayName)
        .getOrElse(repository.getBranch),
    branchNameProperty)
  val visibleProperty = Bindings.createBooleanBinding(
    () =>
      config.knownBranches
        .find(_.name == repository.getBranch)
        .forall(_.state != BranchState.Hidden),
    branchNameProperty)

  val manualVisible = BooleanProperty(true)

  val wrapAt = 400

  stage = new PrimaryStage {
    //    initStyle(StageStyle.Unified)
    initStyle(StageStyle.Transparent)
    title = "Presentation"
    x = config.left
    y = config.top
    maxWidth = config.width
    maxHeight = config.height

    scene = new Scene {
      stylesheets = Seq(config.stylePath)
//      fill <== when (visibleProperty) choose(Color.rgb(38, 38, 38)) otherwise Color.rgb(38, 38, 38, 1d)
//      fill = Color.rgb(38, 38, 38)
      opacity <== when(manualVisible && visibleProperty) choose (1 - config.transparency) otherwise 0d
      content = new VBox {
        id = "main"
//        padding = Insets(50, 80, 50, 80)
        children = Seq(
          new Text {
            id = "name"
            text <== nameProperty
            style = "-fx-font: normal bold 40pt sans-serif"
            onMouseClicked = (t: input.MouseEvent) => {
              alwaysOnTop = !alwaysOnTop.value
            }
//            fill = new LinearGradient(
//              endX = 0,
//              stops = Stops(Red, DarkRed))
            wrappingWidth = wrapAt
          },
          /*new ScrollPane {
            content = */new Text {
            id = "description"
              text <== descriptionProperty
              style = "-fx-font: 14pt sans-serif"
//              fill = new LinearGradient(
//                endX = 0,
//                stops = Stops(White, DarkGray)
//              )
//              effect = new DropShadow {
//                color = DarkGray
//                radius = 15
//                spread = 0.25
//              }
              wrappingWidth = wrapAt
            }
          /*}*/
        )
      }
    }
    onCloseRequest.addListener(_ => watchService.close())
    alwaysOnTop = true
  }
}
