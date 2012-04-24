package com.bimbr.clisson.client

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import javax.servlet.http.HttpServletResponse.{ SC_OK, SC_BAD_REQUEST }
import javax.servlet.ServletInputStream

import org.mortbay.jetty.handler.AbstractHandler
import org.mortbay.jetty.Server

class TestServer(port: Int) {
  var requestReceived: Option[(String, String, String)] = None
  private val jetty = new Server(port)
  jetty setHandler handler
  private val receivedLatch = new CountDownLatch(1)
    
  private object handler extends AbstractHandler {
    def handle(target: String, httpRequest: HttpServletRequest, httpResponse: HttpServletResponse, dispatch: Int) = {
      requestReceived = relevantPartsOf(httpRequest)
      httpResponse setContentType "text/plain"
      httpResponse.setStatus(requestReceived match {
        case Some((_, TestServer.ErrorUri, _)) => SC_BAD_REQUEST
        case _                                 => SC_OK
      })
      httpRequest.asInstanceOf[org.mortbay.jetty.Request] setHandled true
      receivedLatch.countDown()
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
  
  def waitUntilRequestReceived(timeoutMs: Int) = receivedLatch.await(timeoutMs, MILLISECONDS)
}

object TestServer {
  val ErrorUri = "/trigger/error/response"
    
  def withServerOn(port: Int)(test: TestServer => org.specs2.execute.Result) = {
    val server = new TestServer(port) start ()
    try {
      test(server)
    } finally {
      server stop ()
    }
  }
}