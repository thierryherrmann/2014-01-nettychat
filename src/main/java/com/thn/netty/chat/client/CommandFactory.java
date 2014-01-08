package com.thn.netty.chat.client;

import com.thn.netty.chat.client.CommandReader.BadCommandException;
import com.thn.netty.chat.primitive.Command;

/**
 * Base factory to create {@link Command} instances. Captures parameters to present them in a text UI.  
 * @author Thierry Herrmann
 */
public abstract class CommandFactory {
    private final String mName;
    private final String[] mArgs;
    
    public CommandFactory(String aName, String[] aArgs) {
        mName = aName;
        mArgs = aArgs;
    }
    
    public final String getPrompt() {
        StringBuilder builder = new StringBuilder(mName);
        builder.append(' ');
        for (int i = 0; i < mArgs.length; i++) {
            builder.append('<');
            builder.append(mArgs[i]);
            builder.append('>');
            if (i < mArgs.length-1) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }
    
    /**
     * Create a {@link Command} instance.
     * @param aArgs arguments to be passed to the abstract {@link #createCommand(String[])} command.
     * @return the {@link Command} instance.
     * @throws BadCommandException if anything goes wrong.
     */
    public final Command getCommand(String[] aArgs) throws BadCommandException {
        if (aArgs.length != mArgs.length) {
            throw new BadCommandException("wrong number of parameters for command '" + mName + "'");
        }
        return createCommand(aArgs);
    }
    
    /**
     * Abstract method creating the actual {@link Command} instance.
     * @param aArgs arguments. The implementation is responsible to do any string to other primitive type conversion.
     * @return the {@link Command} instance.
     */
    protected abstract Command createCommand(String[] aArgs);
}
