package com.github.aborg0.presentation_helper

import java.io.File
import java.nio.file._

import com.github.aborg0.presentation_helper.config.AppConfig
import com.github.aborg0.presentation_helper.config.AppConfig.BranchState
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import scalafx.application.{JFXApp, Platform}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.binding.Bindings
import scalafx.beans.binding.Bindings._
import scalafx.beans.property.StringProperty
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color._
import scalafx.scene.paint.{Color, LinearGradient, Stops}
import scalafx.scene.text.Text
import scalafx.stage.StageStyle

import scala.jdk.CollectionConverters._


object App extends JFXApp {
  val config = (parameters.unnamed match {
    case Seq() => ConfigSource.default
    case Seq(fileName) => ConfigSource.file(path = fileName)
  }).loadOrThrow[AppConfig]


  val watchService = FileSystems.getDefault.newWatchService
  val gitPath = Paths.get(config.repositoryPath)
  val builder = new FileRepositoryBuilder
  val repository = builder.setGitDir(new File(config.repositoryPath)).readEnvironment.findGitDir // scan environment GIT_* variables
    .build // scan up the file system tree
  var watchKey: WatchKey = null
  gitPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE)
  private val thread = new Thread(() => {
    while ( {
      watchKey = watchService.take();
      watchKey != null
    }) {
      for (event <- watchKey.pollEvents().asScala if event.context().toString == "HEAD") {
        branchNameProperty.value = repository.getBranch
      }
      watchKey.reset()
    }
  }, "file watcher thread")
  thread.setDaemon(true)
  thread.start()

  val branchNameProperty = new StringProperty(repository.getBranch)
  val descriptionProperty = Bindings.createStringBinding(() =>
    config.knownBranches.find(_.name == repository.getBranch).flatMap(_.description).getOrElse(""), branchNameProperty)
  val nameProperty = Bindings.createStringBinding(() => config.knownBranches.find(_.name == repository.getBranch)
    .flatMap(_.displayName).getOrElse(repository.getBranch), branchNameProperty)
  val visibleProperty = Bindings.createBooleanBinding(() => config.knownBranches.find(_.name == repository.getBranch)
    .forall(_.state != BranchState.Hidden), branchNameProperty)

  visibleProperty.addListener(
    (src, old, newValue) => {
      if (newValue != stage.isShowing) {
        Platform.runLater(if (newValue) stage.toBack() else stage.toFront())
      }
    }
  )
  stage = new PrimaryStage {
    //    initStyle(StageStyle.Unified)
    initStyle(StageStyle.Transparent)
    title = "Presentation"
    scene = new Scene {
      stylesheets = Seq(config.stylePath)
//      fill <== when (visibleProperty) choose(Color.rgb(38, 38, 38)) otherwise Color.rgb(38, 38, 38, 1d)
//      fill = Color.rgb(38, 38, 38)
      opacity = 1 - config.transparency
      content = new VBox {
        id = "main"
//        padding = Insets(50, 80, 50, 80)
        children = Seq(
          new Text {
            id = "name"
            text <== nameProperty
            style = "-fx-font: normal bold 100pt sans-serif"
//            fill = new LinearGradient(
//              endX = 0,
//              stops = Stops(Red, DarkRed))
          },
          new ScrollPane{
            content = new Text {
              id = "description"
              text <== descriptionProperty
              style = "-fx-font: italic bold 14pt sans-serif"
//              fill = new LinearGradient(
//                endX = 0,
//                stops = Stops(White, DarkGray)
//              )
//              effect = new DropShadow {
//                color = DarkGray
//                radius = 15
//                spread = 0.25
//              }
              wrappingWidth = 200
            }
          }
        )
      }
    }
    onCloseRequest.addListener(_ => watchService.close())
    alwaysOnTop = true
  }
}
