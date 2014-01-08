package com.thn.netty.chat.primitive;


/**
 * Command to gracefully shutdown the server.
 * @author Thierry Herrmann
 */
public class ShutdownServerRequest extends Command {

    public ShutdownServerRequest(int aCmdId) {
        super(CommandType.SHUTDOWN_SERVER, aCmdId);
    }
}
