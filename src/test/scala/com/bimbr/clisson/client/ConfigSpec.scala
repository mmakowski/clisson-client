package com.bimbr.clisson.client

import scala.collection.JavaConversions._

import org.junit.runner.RunWith
import org.specs2.mutable.{ After, Specification }
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ConfigSpec extends Specification {
  "Config construction" should {
    // these tests modify system properties so can't be run in parallel
    "use classpath's clisson.properties if clisson.config system property is not specified" in synchronized {
      useDefaultConfig()
      val config = Config fromPropertiesFile()
      (config.getHost, config.getPort) mustEqual ("host.from.default.config", 1441)
    }
    "require that config path is prefixed with classpath:// or file://" in synchronized {
      useConfig("clisson.properties")
      Config fromPropertiesFile() must throwAn [IllegalStateException].like {
        case e => e.getMessage must (contain("classpath://") and contain("file://"))
      }
    }
    "report missing file's name when config file is missing from classpath" in synchronized {
      val missingPath = "classpath://missing.properties"
      useConfig(missingPath)
      Config fromPropertiesFile() must throwAn [IllegalStateException].like {
        case e => e.getMessage must contain (missingPath)
      }
    }
    "require that clisson.server.host property is non-empty" in synchronized {
      useConfig("classpath://empty-host.properties")
      Config fromPropertiesFile() must throwAn [Config.ConfigException].like {
        case e => e.getMessage must contain ("clisson.server.host")
      }
    }
    "require that clisson.server.port property is a positive integer" in synchronized {
      useConfig("classpath://negative-port.properties")
      Config fromPropertiesFile() must throwAn [Config.ConfigException].like {
        case e => e.getMessage must contain ("clisson.server.port")
      }
    }
    "report the missing file's name when config is missing from filesystem" in synchronized {
      val missingPath = "file://does/not/exist.properties"
      useConfig(missingPath)
      Config fromPropertiesFile() must throwAn [IllegalStateException].like {
        case e => e.getMessage must contain (missingPath)
      }
    }
    "search the filesystem when path is prefixed with file://" in synchronized {
      useFileConfig("valid.properties")
      val config = Config fromPropertiesFile()
      (config.getHost, config.getPort) mustEqual ("valid.host", 4114)
    }
  }
  
  private def useDefaultConfig() = System clearProperty "clisson.config"
  
  private def useConfig(path: String) = System setProperty ("clisson.config", path)
  
  private def useFileConfig(name: String) = {
    import java.io.{ File, FileOutputStream }
    import java.nio.channels.Channels._
    val tempDir = System getProperty "java.io.tmpdir"
    val src = Thread.currentThread.getContextClassLoader.getResourceAsStream(name);
    val dest = new File(tempDir + "/" + name)
    new FileOutputStream(dest) getChannel() transferFrom(newChannel(src), 0, Long.MaxValue)
    useConfig("file://" + dest)
  } 
  
  trait trees extends After {
    def after = useDefaultConfig()
  }  
}