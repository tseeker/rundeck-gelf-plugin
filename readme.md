# Rundeck GELF Plugin

This is a simple Rundeck streaming Log Writer plugin that will send all log
output to a Graylog server by writing
[GELF](https://docs.graylog.org/en/latest/pages/gelf.html) to a TCP port. Based
on the Rundeck-supported
[Logstash plugin](https://github.com/rundeck-plugins/rundeck-logstash-plugin).

# Installation

Copy the `GelfPlugin.groovy` to your `$RDECK_BASE/libext/` directory for
Rundeck.

Enable the plugin in your `rundeck-config.properties` file:

    rundeck.execution.logs.streamingWriterPlugins=GelfPlugin

# Configure Rundeck

The plugin supports these configuration properties:

* `host` - hostname of the logstash server
* `port` - TCP port to send JSON data to

You can update the your framework/project.properties file to set these
configuration values:

in `framework.properties`:

    framework.plugin.StreamingLogWriter.GelfPlugin.port=9700
    framework.plugin.StreamingLogWriter.GelfPlugin.host=localhost

or in `project.properties`:

    project.plugin.StreamingLogWriter.GelfPlugin.port=9700
    project.plugin.StreamingLogWriter.GelfPlugin.host=localhost
