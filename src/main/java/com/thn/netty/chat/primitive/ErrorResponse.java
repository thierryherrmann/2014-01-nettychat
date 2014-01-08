package com.thn.netty.chat.primitive;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Command to return an error response.
 * @author Thierry Herrmann
 */
public class ErrorResponse extends Command {
    private final Code mCode;
    private final String mDescription;
    
    /**
     * Constructor.
     * @param aCmdId command id.
     * @param aCode error code.
     * @param aDescription description. Optional (can be null).
     */
    public ErrorResponse(int aCmdId, Code aCode, String aDescription) {
        super(CommandType.ERROR, aCmdId);
        mCode = aCode;
        mDescription = aDescription;
    }
    public ErrorResponse(int aCmdId, Code aCode) {
        this(aCmdId, aCode, null);
    }

    public Code getCode() {
        return mCode;
    }

    public String getDescription() {
        return mDescription;
    }
    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .appendSuper(super.toString())
        .append("mCode", mCode)
        .append("mDescription", mDescription)
        .toString();
    }
    
    public static enum Code {
        USER_ALREADY_EXISTS((byte)getIdx()),
        NOT_LOGGED_IN((byte)getIdx()),
        INVALID_USER_OR_PASS((byte)getIdx()),
        BAD_GATEWAY((byte)getIdx()),
        INTERNAL_ERROR((byte)getIdx()),
        TIMEOUT((byte)getIdx()),
        BAD_REQUEST((byte)getIdx()),
        ; 
        static int sIndex;
        private final byte mId;
        static int getIdx(){
            return sIndex++;
        }
        Code(byte aId) {
            mId = aId;
        }
        public byte id() {
            return mId;
        }
        public static Code forId(byte aId) {
            for (Code code : Code.values()) {
                if (code.mId == aId) {
                    return code;
                }
            }
            throw new IllegalArgumentException("bad id: " + ((int) aId));
        }
        @Override
        public String toString() {
            return super.toString() + "(" + (int) mId + ")";
        }
    }
}
