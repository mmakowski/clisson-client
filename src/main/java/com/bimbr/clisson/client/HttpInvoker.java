package com.bimbr.clisson.client;

/**
 * Handles the interaction with the server over HTTP.
 *  
 * @author mmakowski
 * @since 1.0.0
 */
interface HttpInvoker {
 /**
  * Issues a POST request to the server.
  * @param uri the URI of the request
  * @param content the body of the request
  */
 void post(String uri, String content);
}
