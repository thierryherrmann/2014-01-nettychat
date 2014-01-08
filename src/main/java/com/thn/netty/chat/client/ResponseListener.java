package com.thn.netty.chat.client;

import com.thn.netty.chat.primitive.Command;

/**
 * Callback to handle responses from the server.
 * @author Thierry Herrmann
 */
public interface ResponseListener {
    /**
     * Invoked when a command received from the server has the same ID as a command sent earlier to the server. The
     * implementer can do whatever is necessary to process the command.
     * @param aResponse response. Never null.
     */
    void onResponse(Command aResponse);
    /**
     * Returns the timeout in milliseconds that this response listener will wait before invoking 
     * {@link #onTimeout(Command)}.
     * @return timeout in milliseconds.
     */
    long getTimeoutMillis();
    /**
     * Invoked when the timeout was reached before a response was received. If the response is received after the
     * timeout, no ID will be found in the client for this response which will then be considered as a notification.
     * The type of the response will tell if it's a valid notification: if yes it will be processed as such, otherwise
     * it'll be considered as a late response and will be dropped.
     * @param aRequest request for which no response was received before the timeout.
     */
    void onTimeout(Command aRequest);
}
