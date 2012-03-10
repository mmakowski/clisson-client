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
  "AsyncHttpRecorder" should {
    "require non-null invoker on construction" in {
      new AsyncHttpRecorder(SrcId, null, Clock) must throwAn [IllegalArgumentException]
    }
    "require non-empty source id on construction" in {
      new AsyncHttpRecorder("", Invoker, Clock) must throwAn [IllegalArgumentException]
    }
    "send a POST request with checkpoint event JSON to /event when checkpoint() is called" in {
      val invoker = mock[HttpInvoker]
      recorder(invoker) checkpoint (MsgId, Description)
      there was one(invoker).post("/event", Json.jsonFor(Checkpoint)) 
    }
    "send a POST request with generic event JSON to /event when event() is called" in {
      val invoker = mock[HttpInvoker]
      recorder(invoker) event (InputMsgIds, OutputMsgIds, Description)
      there was one(invoker).post("/event", Json.jsonFor(GenericEvent)) 
    }
  }

  val Invoker = mock[HttpInvoker]
  val Timestamp = new java.util.Date
  val Clock = mock[Clock]
  Clock.getTime() returns Timestamp
  val Host = "localhost"
  val PortBase = 31500
  val SrcId = "srcId"
  def recorder(invoker: HttpInvoker) = new AsyncHttpRecorder(SrcId, invoker, Clock)

  val MsgId = "msg-1"
  val Description = "test event"
  val Checkpoint = new Event(SrcId, Timestamp, Set(MsgId), Set(MsgId), Description)
  val InputMsgIds = Set("msg-1", "msg-2")
  val OutputMsgIds = Set("msg-3", "msg-4")
  val GenericEvent = new Event(SrcId, Timestamp, InputMsgIds, OutputMsgIds, Description)
}