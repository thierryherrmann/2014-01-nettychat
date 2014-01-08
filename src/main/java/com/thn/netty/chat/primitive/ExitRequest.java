package com.thn.netty.chat.primitive;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Command to gracefully shutdown the client.
 * @author Thierry Herrmann
 */
public class ExitRequest extends Command {

    public ExitRequest(int aCmdId) {
        super(CommandType.EXIT, aCmdId);
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
