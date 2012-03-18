package com.bimbr.clisson.client.log4j

import scala.collection.JavaConversions._
import org.apache.log4j.spi.LoggingEvent
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import com.bimbr.clisson.client.Recorder
import com.bimbr.clisson.protocol.{ Event, Json }
import com.bimbr.util.Clock
import com.bimbr.clisson.client.globally
import com.bimbr.clisson.client.ConfigSpec.useConfig
import com.bimbr.clisson.client.TestServer.withServerOn

@RunWith(classOf[JUnitRunner])
class ClissonAppenderSpec extends Specification with Mockito {
  "ClissonAppender construction" should {
    "use event transformation class defined in the config" in globally.synchronized {
      useConfig("classpath://log4j-valid.properties")
      TestTransformation.constructed = false
      new ClissonAppender
      TestTransformation.constructed mustEqual true
    }
  }
  "ClissonAppender" should {
    "record Clisson event built from log4j Event using supplied EventTransformation" in {
      val rec = recorder
      appender(rec).doAppend(Log4jEvent)
      there was one (rec).event(ClissonEvent)
    }
    "ignore events deemed ignorable by the transformation" in {
      val rec = recorder
      appender(rec).doAppend(IgnorableLog4jEvent)
      there was no (rec).event(any[Event])
    }
  }
  
  val Transformation = mock[EventTransformation]
  def recorder = mock[Recorder]
  def appender(recorder: Recorder) = new ClissonAppender(Transformation, recorder)
  
  val Log4jEvent = mock[LoggingEvent]
  val IgnorableLog4jEvent = mock[LoggingEvent]
  val ClissonEvent = mock[Event]

  Transformation.perform(Log4jEvent) returns ClissonEvent
  Transformation.perform(IgnorableLog4jEvent) throws new EventTransformation.IgnoreEventException 
}

class TestTransformation extends EventTransformation {
  import TestTransformation._
  constructed = true
  def perform(log4jEvent: LoggingEvent) = Result
}

object TestTransformation {
  val Result = new Event("appender-test", new java.util.Date, Set("msg-1"), Set("msg-2"), "unique!")
  var constructed = false
}
