package com.thn.netty.chat.primitive;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Base class for commands.
 * @author Thierry Herrmann
 */
public abstract class Command {
    private final CommandType mType;
    private final int mId;
    
    /**
     * Constructor.
     * @param aType command type. Used (among other things) to find the command's encoder/decoder.
     * @param aId command id. Used to reconcile requests with responses.
     */
    public Command(CommandType aType, int aId) {
        mType = aType;
        mId = aId;
    }
    
    public CommandType getType() {
        return mType;
    }

    public int getId() {
        return mId;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .appendSuper(super.toString())
        .append("mType", mType)
        .append("mId", mId)
        .toString();
    }
}
