package com.bimbr.clisson.client;

import static com.bimbr.clisson.protocol.Types.id;
import static com.bimbr.clisson.util.Arguments.nonEmpty;
import static com.bimbr.clisson.util.Arguments.nonNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.bimbr.clisson.protocol.Checkpoint;
import com.bimbr.clisson.protocol.Event;
import com.bimbr.clisson.protocol.EventHeader;
import com.bimbr.clisson.protocol.Json;
import com.bimbr.util.Clock;

/**
 * A {@link Trail} implementation that sends events over HTTP as they are reported by the calling code.
 * 
 * @author mmakowski
 * @since 1.0.0
 */
public class SimpleHttpTrail implements Trail {
    private final String serverHost;
    private final int serverPort;
    private final String sourceId;

    private final Clock clock;

    /**
     * @param serverHost the host name of Clisson server, not including protocol and port, e.g.
     *                      {@code www.example.com} 
     * @param serverPort the port on which Clisson server is listening
     * @param sourceId the id of the component that is the source of events
     */
    public SimpleHttpTrail(final String serverHost,
                           final int    serverPort,
                           final String sourceId) {
        this(serverHost, serverPort, sourceId, new Clock());
    }
    
    // allows to set custom clock (for unit testing)
    SimpleHttpTrail(final String serverHost,
                    final int    serverPort,
                    final String sourceId,
                    final Clock  clock) {
        this.serverHost = nonEmpty(serverHost, "serverHost");
        this.serverPort = serverPort;
        this.sourceId = nonEmpty(sourceId, "sourceId");
        this.clock = nonNull(clock, "clock");
    }
    
    /**
     * @see Trail#checkpoint(int, String, String)
     */
    public void checkpoint(int priority, String messageId, String description) {
        final EventHeader header = new EventHeader(sourceId, clock.getTime(), priority);
        final Checkpoint event = new Checkpoint(header, messageId, description);
        sendEvent(event);
    }

    private void sendEvent(Event event) {
        try {
            final HttpPost request = post("/event/" + id(event.getClass()));
            request.setEntity(entityFor(event));
            sendRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(HttpPost request) throws ClientProtocolException, IOException {
        final HttpClient client = new DefaultHttpClient();
        final HttpResponse response = client.execute(request);
        if (response.getStatusLine().getStatusCode() >= 400) {
            // TODO: better diagnostics
            System.err.println(response);
        }
    }

    private static HttpEntity entityFor(Event event) throws UnsupportedEncodingException {
        return new StringEntity(Json.jsonFor(event), "UTF-8");
    }

    private HttpPost post(String path) throws URISyntaxException {
        return new HttpPost(URIUtils.createURI("http", serverHost, serverPort, path, "", null));
    }

}
