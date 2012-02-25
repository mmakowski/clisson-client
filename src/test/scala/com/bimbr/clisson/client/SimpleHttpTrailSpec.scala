package com.bimbr.clisson.client

import org.mortbay.jetty.handler.AbstractHandler
import org.mortbay.jetty.Server
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import com.bimbr.clisson.protocol.CheckpointEvent
import com.bimbr.clisson.protocol.EventHeader
import com.bimbr.clisson.protocol.Json
import com.bimbr.util.Clock

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletInputStream

class SimpleHttpTrailSpec extends Specification with Mockito {
  "SimpleHttpTrail" should {
    
    "require non-empty sever hostname on construction" in {
      new SimpleHttpTrail("", Port, SrcId) must throwAn [IllegalArgumentException]
    }
    
    "require non-empty source id on construction" in {
      new SimpleHttpTrail(Host, Port, "") must throwAn [IllegalArgumentException]
    }
    
    "send a POST request with event JSON to /checkpointevent when checkpoint() is called" in {
      val server = new TestServer() start ()
      try {
        Trail checkpoint (Priority, MsgId, Description)
        server.requestReceived mustEqual Some(("POST", "/checkpointevent", Json.jsonFor(Checkpoint))) 
      } finally {
        server stop ()
      }
    }
  }
  
  class TestServer {
    var requestReceived: Option[(String, String, String)] = None
    private val jetty = new Server(Port)
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
  val Port = 31401
  val SrcId = "srcId";
  val Trail = new SimpleHttpTrail(Host, Port, SrcId, Clock)
  val Priority = 13
  val MsgId = "msg-1"
  val Description = "test checkpoint"
  val Checkpoint = new CheckpointEvent(new EventHeader(SrcId, Timestamp, Priority), MsgId, Description)
}