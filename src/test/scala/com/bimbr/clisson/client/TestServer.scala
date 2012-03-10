package com.bimbr.clisson.client

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import javax.servlet.ServletInputStream

import org.mortbay.jetty.handler.AbstractHandler
import org.mortbay.jetty.Server

class TestServer(port: Int) {
  var requestReceived: Option[(String, String, String)] = None
  private val jetty = new Server(port)
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

object TestServer {
  def withServerOn(port: Int)(test: TestServer => org.specs2.execute.Result) = {
    val server = new TestServer(port) start ()
    try {
      test(server)
    } finally {
      server stop ()
    }
  }
}