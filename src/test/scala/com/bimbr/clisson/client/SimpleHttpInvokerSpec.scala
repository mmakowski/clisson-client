package com.bimbr.clisson.client

import scala.collection.JavaConversions._

import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import com.bimbr.clisson.protocol.{ Event, Json }
import com.bimbr.util.Clock

import TestServer.withServerOn

@RunWith(classOf[JUnitRunner])
class HttpInvokerSpec extends Specification with Mockito {
  "HttpInvoker construction" should {
    "require non-empty sever hostname" in {
      new SimpleHttpInvoker("", PortBase) must throwAn [IllegalArgumentException]
    }
    "require a positive port number" in {
      new SimpleHttpInvoker(Host, 0) must throwAn [IllegalArgumentException]
    }
  }
  "HttpInvoker" should {
    "send a POST request with specified content to specified URI when post() is called" in withServerOn(port(1)) { server =>
      invoker(1) post (Uri, Content)
      server.requestReceived mustEqual Some(("POST", Uri, Content)) 
    }
    "throw a RuntimeException when status code is >= 400" in withServerOn(port(2)) { server =>
      invoker(2).post(TestServer.ErrorUri, Content) must throwA [RuntimeException] 
    }
    "throw a RuntimeException when the URI format is invalid" in {
      invoker(3).post(InvalidUri, Content) must throwA [RuntimeException]
    }
  }
  
  val Host = "localhost"
  val PortBase = 31500
  def port(instance: Int) = PortBase + instance
  def invoker(instance: Int) = new SimpleHttpInvoker(Host, port(instance))
  val Uri = "/some/uri"
  val InvalidUri = "a#adf#adsf#"
  val Content = """some content -- newlines are not guaranteed to be preserved, but UTF-8 characters like Թ should!"""
}