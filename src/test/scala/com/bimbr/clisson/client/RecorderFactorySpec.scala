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
    "create a recorder capable of submitting a valid event to the server" in globally.synchronized { 
      withServerOn(37171) { server =>
        System setProperty ("clisson.config", "classpath://local-test.properties")
        val recorder = RecorderFactory.getRecorder(SrcId)
        System clearProperty "clisson.config"
        recorder.event(InputMsgIds, OutputMsgIds, Description)
        server.waitUntilRequestReceived(5000)
        server.requestReceived must beSome.like {
          case ("POST", "/event", str) => Json.fromJson[Event](str, classOf[Event]).getDescription mustEqual Description  
        }
      }
    }
    "return the same instance of recorder whenever a recorder with specific source id is requested" in globally.synchronized {
      System clearProperty "clisson.config"
      val recorder1 = RecorderFactory.getRecorder(SrcId)
      val recorder2 = RecorderFactory.getRecorder(SrcId)
      recorder1 must be (recorder2)
    }
  }

  val SrcId = "factory-test"
  val Description = "test event"
  val InputMsgIds = Set("msg-1", "msg-2")
  val OutputMsgIds = Set("msg-3", "msg-4")
}