import com.dtolabs.rundeck.plugins.logging.StreamingLogWriterPlugin;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dtolabs.rundeck.core.logging.LogEvent;
import com.dtolabs.rundeck.core.logging.LogLevel;

/**
 * Opens a TCP connection, and writes GELF-formatted event messages to the socket
 */
rundeckPlugin(StreamingLogWriterPlugin){
    configuration{
        host defaultValue:"localhost", required:true, description: "Hostname to connect to"
        port required:true, description: "Port to connect to", type: 'Integer'
    }
    /**
     * open the socket, prepare some metadata
     */
    open { Map execution, Map config ->
        def socket = new Socket(config.host, config.port.toInteger());
        def socketStream = socket.getOutputStream();
        def e2 = [
            version:"1.1",
            host:execution.serverUrl.replaceFirst( /^https?:/ , '' ).replaceAll( /\// , '' ),
        ]
        execution.each{ e2["_execution.${it.key}"]=it.value }
        def execttl = execution.project + ' > ' + execution.name
        def json=new ObjectMapper()

        [socket:socket, count:0, executionid:execution.execid, executionttl: execttl, write:{
            socketStream<< json.writeValueAsString(e2 + it) + "\u0000"
        }]
    }

    /**
     * write the log event and metadata as json to the socket
     */
    addEvent { Map context, LogEvent event->

        if ( context.count == 0 ) {
            def start_data = [
                _recType:"start",
                _line:0,
                level:4,
                short_message: 'Execution '+context.executionid+' started.'
            ]
            context.write start_data
        }

        context.count++

        def emeta=[:]

        event.metadata?.each{ emeta["_event.${it.key}"]=it.value }

        def data= emeta + [
            _recType:"log",
            _line:context.count,
            _eventType:event.eventType,
            level:event.loglevel.ordinal() + 3,
            short_message:event.message ?: ( context.executionttl + ' > ' + emeta[ '_event.step.label' ] + ' (' + event.eventType + ')' ),
        ]

        context.write data
    }
    /**
     * close the socket
     */
    close {
        context.count++
        def end_data = [
            _recType:"end",
            _line:context.count,
            level:4,
            short_message: 'Execution '+context.executionid+' finished.'
        ]
        context.write end_data
        context.socket.close();
    }
}
