package com.bimbr.clisson.client;

import static com.bimbr.clisson.util.Arguments.nonEmpty;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the interaction with the server over HTTP.
 *  
 * @author mmakowski
 * @since 1.0.0
 */
class HttpInvoker {
    private static final Logger logger = LoggerFactory.getLogger(HttpInvoker.class);

    private final String serverHost;
    private final int serverPort;
    
    /**
     * @param serverHost the host name of Clisson server, not including protocol and port, e.g.
     *                   {@code www.example.com} 
     * @param serverPort the port on which Clisson server is listening
     * @param sourceId the id of the component that is the source of events
     */
    public HttpInvoker(final String serverHost,
                       final int    serverPort) {
        this.serverHost = nonEmpty(serverHost, "serverHost");
        this.serverPort = positive(serverPort, "serverPort");
    }

    private static int positive(int arg, String argName) {
        if (arg <= 0) throw new IllegalArgumentException(argName + " must be positive but was " + arg);
        return arg;
    }

    public void post(String uri, String content) {
        HttpPost request;
        try {
            request = post(uri);
            request.setEntity(entityFor(content));
            sendRequest(request);
        } catch (URISyntaxException e) {
            logger.error("invalid URI: " + uri, e);
        } catch (IOException e) {
            logger.error("error when posting to " + url(uri), e);
        }
    }

    private String url(String uri) {
        return "http://" + serverHost + ":" + serverPort + uri;
    }

    private void sendRequest(HttpPost request) throws ClientProtocolException, IOException {
        final HttpClient client = new DefaultHttpClient();
        final HttpResponse response = client.execute(request);
        if (response.getStatusLine().getStatusCode() >= 400) {
            logger.error("response to " + request.getMethod() + " " + request.getURI() + ": " + response);
        }
    }

    private static HttpEntity entityFor(String content) throws UnsupportedEncodingException {
        return new StringEntity(content, "UTF-8");
    }

    private HttpPost post(String path) throws URISyntaxException {
        return new HttpPost(URIUtils.createURI("http", serverHost, serverPort, path, "", null));
    }

}
