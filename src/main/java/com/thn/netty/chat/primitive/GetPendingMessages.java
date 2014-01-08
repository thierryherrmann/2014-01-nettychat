package com.thn.netty.chat.primitive;

/**
 * Command to get all the messages sent to the current user while he was offline. 
 * @author Thierry Herrmann
 */
public class GetPendingMessages extends Command  {
    public GetPendingMessages(int aCmdId) {
        super(CommandType.GET_PENDING_MESSAGES, aCmdId);
    }
}
