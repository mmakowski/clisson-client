package com.bimbr.clisson.client

import scala.collection.JavaConversions._

import org.junit.runner.RunWith
import org.specs2.mutable.{ After, Specification }
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ConfigSpec extends Specification {
  import ConfigSpec._
  "Config construction" should {
    // these tests modify system properties so can't be run in parallel
    "use classpath's clisson.properties if clisson.config system property is not specified" in globally.synchronized {
      useDefaultConfig()
      val config = Config fromPropertiesFile()
      (config.getHost, config.getPort) mustEqual ("host.from.default.config", 1441)
    }
    "require that config path is prefixed with classpath:// or file://" in globally.synchronized {
      useConfig("clisson.properties")
      Config fromPropertiesFile() must throwAn [IllegalStateException].like {
        case e => e.getMessage must (contain("classpath://") and contain("file://"))
      }
    }
    "create config with disabled recording when config is missing from classpath" in globally.synchronized {
      val missingPath = "classpath://missing.properties"
      useConfig(missingPath)
      val config = Config fromPropertiesFile() 
      config.isRecordingEnabled mustEqual false
    }
    "require that clisson.server.host property is non-empty" in globally.synchronized {
      useConfig("classpath://empty-host.properties")
      Config fromPropertiesFile() must throwAn [Config.ConfigException].like {
        case e => e.getMessage must contain ("clisson.server.host")
      }
    }
    "require that clisson.server.port property is a positive integer" in globally.synchronized {
      useConfig("classpath://negative-port.properties")
      Config fromPropertiesFile() must throwAn [Config.ConfigException].like {
        case e => e.getMessage must contain ("clisson.server.port")
      }
    }
    "require that clisson.componentId property is non-empty" in globally.synchronized {
      useConfig("classpath://empty-componentId.properties")
      Config fromPropertiesFile() must throwAn [Config.ConfigException].like {
        case e => e.getMessage must contain ("clisson.componentId")
      }
    }
    "create config with disabled recording when config is missing from filesystem" in globally.synchronized {
      val missingPath = "file://does/not/exist.properties"
      useConfig(missingPath)
      val config = Config fromPropertiesFile() 
      config.isRecordingEnabled mustEqual false
    }
    "search the filesystem when path is prefixed with file://" in globally.synchronized {
      useFileConfig("valid.properties")
      val config = Config fromPropertiesFile()
      (config.getHost, config.getPort) mustEqual ("valid.host", 4114)
    }
  }
  "Config" should {
    "have isEnabled set to true if clisson.record.enabled property is not specified" in globally.synchronized {
      useDefaultConfig()
      val config = Config fromPropertiesFile()
      config.isRecordingEnabled mustEqual (true)
    }
    "have isEnabled set to false if clisson.record.enabled property is set to false" in globally.synchronized {
      useFileConfig("disabled.properties")
      val config = Config fromPropertiesFile()
      config.isRecordingEnabled mustEqual (false)
    }
  }
  
  private def useFileConfig(name: String) = {
    import java.io.{ File, FileOutputStream }
    import java.nio.channels.Channels._
    val tempDir = System getProperty "java.io.tmpdir"
    val src = Thread.currentThread.getContextClassLoader.getResourceAsStream(name);
    val dest = new File(tempDir + "/" + name)
    if (dest.exists) dest.delete()
    new FileOutputStream(dest) getChannel() transferFrom(newChannel(src), 0, Long.MaxValue)
    useConfig("file://" + dest)
  } 
  
  trait trees extends After {
    def after = useDefaultConfig()
  }  
}

object ConfigSpec {
  def useDefaultConfig() = System clearProperty "clisson.config"
  
  def useConfig(path: String) = System setProperty ("clisson.config", path)
}