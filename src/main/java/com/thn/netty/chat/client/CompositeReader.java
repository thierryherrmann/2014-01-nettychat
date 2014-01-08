package com.thn.netty.chat.client;

import java.util.ArrayList;
import java.util.List;

import com.thn.netty.chat.primitive.Command;

/**
 * {@link CommandReader} combining several readers. When the stream of commands of one reader has been reached,
 * commands are read from the next {@link CommandReader}.  
 * @author Thierry Herrmann
 */
public class CompositeReader implements CommandReader {

    private final List<CommandReader> mReaders = new ArrayList<>();
    private int mReaderIndex;
    
    
    /**
     * {@link CompositeReader} constructor.
     * @param aReaders contained by this composite reader. Others can be added later with 
     * {@link #addReader(CommandReader)}.
     */
    public CompositeReader(CommandReader... aReaders) {
        for (CommandReader reader : aReaders) {
            addReader(reader);
        }
    }

    /**
     * Sets the next reader to be used when this reader has no more commands to return. In this case, 
     * {@link #getCommand()} of this reader automatically returns <code>aReader</code>'s {@link #getCommand()}.
     * @param aReader chained reader to be used when this reader has no more command to return.
     */
    public final void addReader(CommandReader aReader) {
        if (aReader == null) {
            throw new NullPointerException("null reader");
        }
        mReaders.add(aReader);
    }

    @Override
    public Command getCommand() throws BadCommandException {
        if (mReaderIndex == mReaders.size()) {
            return null;
        }
        Command cmd = mReaders.get(mReaderIndex).getCommand();
        if (cmd == null) {
            mReaderIndex++;
            return getCommand();
        }
        return cmd;
    }
}
