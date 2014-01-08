package com.thn.netty.chat.primitive;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Generic successful response to confirm a command was successfully processed.
 * Used when a response contains no payload.
 * 
 * @author Thierry Herrmann
 */
public class OkResponse extends Command {
    public  OkResponse(int aCmdId) {
        super(CommandType.OK, aCmdId);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .appendSuper(super.toString())
        .toString();
    }
}
