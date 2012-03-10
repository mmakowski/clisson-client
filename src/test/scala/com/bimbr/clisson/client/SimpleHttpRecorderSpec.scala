package com.bimbr.clisson.client

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletInputStream
import scala.collection.JavaConversions._

import org.junit.runner.RunWith
import org.mortbay.jetty.handler.AbstractHandler
import org.mortbay.jetty.Server
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import com.bimbr.clisson.protocol.{ Event, Json }
import com.bimbr.util.Clock

@RunWith(classOf[JUnitRunner])
class SimpleHttpRecorderSpec extends Specification with Mockito {
  "SimpleHttpTrail" should {
    "require non-empty sever hostname on construction" in {
      new SimpleHttpRecorder("", PortBase, SrcId) must throwAn [IllegalArgumentException]
    }
    "require non-empty source id on construction" in {
      new SimpleHttpRecorder(Host, PortBase, "") must throwAn [IllegalArgumentException]
    }
    "send a POST request with checkpoint event JSON to /event when checkpoint() is called" in {
      val server = new TestServer(1) start ()
      try {
        recorder(1) checkpoint (MsgId, Description)
        server.requestReceived mustEqual Some(("POST", "/event", Json.jsonFor(Checkpoint))) 
      } finally {
        server stop ()
      }
    }
    "send a POST request with generic event JSON to /event when event() is called" in {
      val server = new TestServer(2) start ()
      try {
        recorder(2) event (InputMsgIds, OutputMsgIds, Description)
        server.requestReceived mustEqual Some(("POST", "/event", Json.jsonFor(GenericEvent))) 
      } finally {
        server stop ()
      }
    }
  }
  
  class TestServer(instance: Int) {
    var requestReceived: Option[(String, String, String)] = None
    private val jetty = new Server(PortBase + instance)
    jetty setHandler handler
    
    private object handler extends AbstractHandler {
      def handle(target: String, httpRequest: HttpServletRequest, httpResponse: HttpServletResponse, dispatch: Int) = {
        requestReceived = relevantPartsOf(httpRequest)
        httpResponse setContentType "text/plain"
        httpRequest.asInstanceOf[org.mortbay.jetty.Request] setHandled true
      }
    }
    
    private def relevantPartsOf(httpRequest: HttpServletRequest) = Some((
      httpRequest getMethod,
      httpRequest getPathInfo,
      read(httpRequest getInputStream)
    )) 
    
    private def read(input: ServletInputStream) = scala.io.Source.fromInputStream(input).getLines.mkString
    
    def start() = {
      jetty.start()
      this
    }
    
    def stop() = jetty.stop()
  }
  
  val Timestamp = new java.util.Date
  val Clock = mock[Clock]
  Clock.getTime() returns Timestamp
  val Host = "localhost"
  val PortBase = 31400
  val SrcId = "srcId";
  def recorder(instance: Int) = new SimpleHttpRecorder(Host, PortBase + instance, SrcId, Clock)

  val MsgId = "msg-1"
  val Description = "test checkpoint"
  val Checkpoint = new Event(SrcId, Timestamp, Set(MsgId), Set(MsgId), Description)
  val InputMsgIds = Set("msg-1", "msg-2")
  val OutputMsgIds = Set("msg-3", "msg-4")
  val GenericEvent = new Event(SrcId, Timestamp, InputMsgIds, OutputMsgIds, Description)
}