package com.github.aborg0.presentation_helper

import org.scalatest.wordspec.AnyWordSpec

class CheckConfigTest extends AnyWordSpec {
  "CheckConfig" should {
    "not throw exception for its main without arguments" in {
      CheckConfig.main(Array[String]())
    }
  }
}
