package com.thn.netty.chat.client;

import java.util.LinkedList;

import com.thn.netty.chat.primitive.Command;

/**
 * {@link CommandReader} implementation reading pre-recorded commands.
 * @author Thierry Herrmann
 */
public class RecordedCommandReader implements CommandReader {
    
    private final LinkedList<Command> mCommands = new LinkedList<>();
    
    /**
     * Records a command to be later returned by {@link #getCommand()}.
     * @param aCmd command.
     * @return this instance to allow calls chaining.
     */
    public RecordedCommandReader add(Command aCmd) {
        mCommands.add(aCmd);
        return this;
    }
    
    @Override
    public Command getCommand() throws BadCommandException {
        if (mCommands.isEmpty()) {
            return null;
        }
        return mCommands.removeFirst();
    }
}
