Clisson Client
==============

Clisson is an event database for message processing systems. Clisson Client is a Java library used for recording events.

Adding dependency
-----------------

In your Maven/SBT/ivy/whatever file add the following dependency:

* group: `com.bimbr`
* artifactId: `clisson-client`
* version: `0.3.0`

Configuration
-------------

The config is specified in a properties file pointed to by `clisson.config` system property. If the property is not specified the config is loaded from `classpath://clisson.properties` file. The path to the config file must be prefixed with protocol, `classpath://` or `file://`that determines how the file is searched for. Examples of paths:
 
 * `classpath://conf/clisson-test.properties`
 * `file://c:/myapp/conf/clisson.properties`

The config file must contain the following properties:

* `clisson.componentId` - the component id that will appear against events logged from this app 
* `clisson.server.host` - the host name of [Clisson server](https://github.com/mmakowski/clisson-server)
* `clisson.server.port` - the port on which Clisson server listens

The config file may also contain:

* `clisson.record.enabled` - whether sending of events to the server is enabled (default: `true`)

If config file is not found, the application will run with event recording disabled.

Usage
-----

In classes where you'd like events recorded, first construct an instance of `Recorder`:

    final Recorder record = RecorderFactory.getRecorder();

and use it to record events:

    record.checkpoint("msg001", "received!");
    
See the javadoc of `Recorder` for other methods of recording events. 

