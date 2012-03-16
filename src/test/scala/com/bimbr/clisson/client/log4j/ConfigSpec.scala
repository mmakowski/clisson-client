package com.bimbr.clisson.client.log4j

import scala.collection.JavaConversions._

import org.junit.runner.RunWith
import org.specs2.mutable.{ After, Specification }
import org.specs2.runner.JUnitRunner

import com.bimbr.clisson.client.globally
import com.bimbr.clisson.client.Config.ConfigException

@RunWith(classOf[JUnitRunner])
class ConfigSpec extends com.bimbr.clisson.client.ConfigSpec {
  import com.bimbr.clisson.client.ConfigSpec._
  
  "log4j Config construction" should {
    // these tests modify system properties so can't be run in parallel
    "require that clisson.log4j.eventTransformation property points to a class implementing EventTransformation" in globally.synchronized {
      useConfig("classpath://log4j-wrong-transformation.properties")
      Config fromPropertiesFile() must throwA [ConfigException].like {
        case e => e.getMessage must contain ("clisson.log4j.eventTransformation")
      }
    }
  }
  
  trait trees extends After {
    def after = useDefaultConfig()
  }  
}
