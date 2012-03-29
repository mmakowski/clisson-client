package com.bimbr.clisson.client

import java.util.concurrent.CountDownLatch

import scala.collection.JavaConversions._

import org.junit.runner.RunWith
import org.slf4j.Logger
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import com.bimbr.clisson.protocol.{ Event, Json }
import com.bimbr.util.Clock

@RunWith(classOf[JUnitRunner])
class AsyncHttpRecorderSpec extends Specification with Mockito {
  "AsyncHttpRecorder construction" should {
    "require non-empty source" in {
      new AsyncHttpRecorder("", Invoker, BufferSize, Clock) must throwAn [IllegalArgumentException]
    }
    "require non-null invoker" in {
      new AsyncHttpRecorder(SrcId, null, BufferSize, Clock) must throwAn [IllegalArgumentException]
    }
    "require positive buffer size" in {
      new AsyncHttpRecorder(SrcId, Invoker, 0, Clock) must throwAn [IllegalArgumentException]
    }
  }
  "AsyncHttpRecorder" should {
    "send a POST request with checkpoint event JSON to /event when checkpoint() is called" in {
      val invoker = mock[HttpInvoker]
      recorder(invoker) checkpoint (MsgId, Description)
      Thread sleep MaxExpectedInvocationDelayMs
      there was one(invoker).post("/event", Json.jsonFor(Checkpoint)) 
    }
    "send a POST request with generic event JSON to /event when event() is called" in {
      val invoker = mock[HttpInvoker]
      recorder(invoker) event (InputMsgIds, OutputMsgIds, Description)
      Thread sleep MaxExpectedInvocationDelayMs
      there was one(invoker).post("/event", Json.jsonFor(GenericEvent))
    }
    "have event() return immediately even when the invoker is slow" in {
      val invoker = new BlockedInvoker
      val callStartTime = System.currentTimeMillis
      recorder(invoker) event (InputMsgIds, OutputMsgIds, Description)
      val elapsedTime = System.currentTimeMillis - callStartTime
      invoker.unblock()
      elapsedTime must beLessThan (MaxExpectedCallTimeMs)
    }
    "throttle buffer full log messages so that the log file is not spammed" in {
      val invoker = new BlockedInvoker
      val logger = mock[Logger]
      val record = recorder(invoker, logger)
      fillBuffer(record)
      recordEventsFor(PeriodThatAllowsTwoLogMessages, record)
      invoker.unblock()
      there were two(logger).warn(anyString)
    } 
    "throttle server invocation error log messages so that the log file is not spammed" in {
      val invoker = failingInvoker()
      val logger = mock[Logger]
      val record = recorder(invoker, logger)
      recordEventsFor(PeriodThatAllowsTwoLogMessages, record)
      there were two(logger).warn(anyString, any[Exception])
    }
  }

  def recordEventsFor(timeMs: Long, record: Recorder) = {
      val startTime = System.currentTimeMillis()
      while (System.currentTimeMillis() - startTime < timeMs) {
        record.event(InputMsgIds, OutputMsgIds, Description)
      }
  } 
  
  val Invoker = mock[HttpInvoker]
  val Timestamp = new java.util.Date
  val Clock = mock[Clock]
  Clock.getTime() returns Timestamp
  val Host = "localhost"
  val PortBase = 31500
  val SrcId = "srcId"
  val BufferSize = 1
  val Logger = mock[Logger]
  val LoggerGagPeriodMs = 750
  val PeriodThatAllowsTwoLogMessages = (1.1 * LoggerGagPeriodMs).toLong
  def recorder(invoker: HttpInvoker, logger: Logger = Logger) = new AsyncHttpRecorder(SrcId, invoker, 1, Clock, logger, LoggerGagPeriodMs)
  
  val MsgId = "msg-1"
  val Description = "test event"
  val Checkpoint = new Event(SrcId, Timestamp, Set(MsgId), Set(MsgId), Description)
  val InputMsgIds = Set("msg-1", "msg-2")
  val OutputMsgIds = Set("msg-3", "msg-4")
  val GenericEvent = new Event(SrcId, Timestamp, InputMsgIds, OutputMsgIds, Description)
  
  class BlockedInvoker extends HttpInvoker {
    val latch = new CountDownLatch(1)
    override def post(s1: String, s2: String) = latch.await()
    def unblock() = latch.countDown()
  }
  
  def failingInvoker() = {
    val invoker = mock[HttpInvoker]
    invoker.post(anyString, anyString) throws (new RuntimeException("test error"))
    invoker
  }
  
  def fillBuffer(record: Recorder) = (0 until 1000) foreach { _ => record.event(InputMsgIds, OutputMsgIds, Description) }
  
  val MaxExpectedInvocationDelayMs = 300
  val MaxExpectedCallTimeMs = 200L
}