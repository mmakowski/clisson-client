package com.bimbr.clisson.client

import scala.collection.JavaConversions._

import org.junit.runner.RunWith
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
      val invoker = slowInvoker(1000)
      val callStartTime = System.currentTimeMillis
      recorder(invoker) event (InputMsgIds, OutputMsgIds, Description)
      val elapsedTime = System.currentTimeMillis - callStartTime
      elapsedTime must beLessThan (MaxExpectedCallTimeMs)
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
  def recorder(invoker: HttpInvoker) = new AsyncHttpRecorder(SrcId, invoker, 1, Clock)
  
  val MsgId = "msg-1"
  val Description = "test event"
  val Checkpoint = new Event(SrcId, Timestamp, Set(MsgId), Set(MsgId), Description)
  val InputMsgIds = Set("msg-1", "msg-2")
  val OutputMsgIds = Set("msg-3", "msg-4")
  val GenericEvent = new Event(SrcId, Timestamp, InputMsgIds, OutputMsgIds, Description)
  
  def slowInvoker(timeToRespondInMillis: Int) = 
    mock[HttpInvoker].post(anyString, anyString) answers {_ => Thread sleep (timeToRespondInMillis) }
  
  val MaxExpectedInvocationDelayMs = 300
  val MaxExpectedCallTimeMs = 200L
}