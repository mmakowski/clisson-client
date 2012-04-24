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
      recordWithPropertiesAndExpect(37171, "classpath://local-test.properties", beSome.like {
          case ("POST", "/event", str) => Json.fromJson[Event](str, classOf[Event]).getDescription mustEqual Description  
        }
      )
    }
    "create a disabled recorder if clisson.record.enabled config property is set to false" in globally.synchronized {
      recordWithPropertiesAndExpect(37172, "classpath://local-test-disabled.properties", beNone)
    }
    "return the same instance of recorder whenever a recorder with specific source id is requested" in globally.synchronized {
      System clearProperty "clisson.config"
      val recorder1 = RecorderFactory.getRecorder(SrcId)
      val recorder2 = RecorderFactory.getRecorder(SrcId)
      recorder1 must be (recorder2)
    }
  }

  def recordWithPropertiesAndExpect(port: Int, propertiesPath: String, beAsExpected: org.specs2.matcher.Matcher[Option[(String, String, String)]]): org.specs2.execute.Result =
    withServerOn(port) { server =>
      RecorderFactory.reset()
      System setProperty ("clisson.config", propertiesPath)
      val recorder = RecorderFactory.getRecorder(SrcId)
      System clearProperty "clisson.config"
      recorder.event(InputMsgIds, OutputMsgIds, Description)
      server.waitUntilRequestReceived(RecordingTimeOutMs)
      server.requestReceived must beAsExpected
    }
  
  val RecordingTimeOutMs = 5000
  val SrcId = "factory-test"
  val Description = "test event"
  val InputMsgIds = Set("msg-1", "msg-2")
  val OutputMsgIds = Set("msg-3", "msg-4")
}