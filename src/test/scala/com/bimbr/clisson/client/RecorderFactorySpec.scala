package com.bimbr.clisson.client

import scala.collection.JavaConversions._

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import com.bimbr.clisson.protocol.{ Event, Json }

import TestServer.withServerOn

@RunWith(classOf[JUnitRunner])
class RecorderFactorySpec extends Specification {
  "RecorderFactory" should {
    "create a recorder capable of submitting a valid event to the server" in withServerOn(37171) { server =>
      System setProperty ("clisson.config", "classpath://local-test.properties")
      val recorder = RecorderFactory.getRecorder(SrcId)
      System clearProperty "clisson.config"
      recorder.event(InputMsgIds, OutputMsgIds, Description)
      Thread sleep 500 // submission is asynchronous, but should be quick
      server.requestReceived must beSome.like {
        case ("POST", "/event", str) => Json.fromJson[Event](str, classOf[Event]).getDescription mustEqual Description  
      }
    }
  }

  val SrcId = "factory-test"
  val Description = "test event"
  val InputMsgIds = Set("msg-1", "msg-2")
  val OutputMsgIds = Set("msg-3", "msg-4")
}