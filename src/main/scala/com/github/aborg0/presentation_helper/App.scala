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
import scalafx.beans.property.StringProperty
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._
import scalafx.scene.paint.{Color, LinearGradient, Stops}
import scalafx.scene.text.Text

import scala.jdk.CollectionConverters._


object App extends JFXApp {
  val config = ConfigSource.default.loadOrThrow[AppConfig]


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
  val visibleProperty = Bindings.createBooleanBinding(() => config.knownBranches.find(_.name == repository.getBranch)
    .forall(_.state != BranchState.Hidden), branchNameProperty).addListener(
    (src, old, newValue) => {
      if (newValue != stage.isShowing) {
        Platform.runLater(if (newValue) stage.show() else stage.hide())
      }
    }
  )
  stage = new PrimaryStage {
    //    initStyle(StageStyle.Unified)
    title = "ScalaFX Hello World"
    scene = new Scene {
      fill = Color.rgb(38, 38, 38)
      content = new HBox {
        padding = Insets(50, 80, 50, 80)
        children = Seq(
          new Text {
            text <== branchNameProperty
            style = "-fx-font: normal bold 100pt sans-serif"
            fill = new LinearGradient(
              endX = 0,
              stops = Stops(Red, DarkRed))
          },
          new Text {
            text <== descriptionProperty
            style = "-fx-font: italic bold 100pt sans-serif"
            fill = new LinearGradient(
              endX = 0,
              stops = Stops(White, DarkGray)
            )
            effect = new DropShadow {
              color = DarkGray
              radius = 15
              spread = 0.25
            }
          }
        )
      }
    }
    onCloseRequest.addListener(_ => watchService.close())
    alwaysOnTop = true
  }
}
