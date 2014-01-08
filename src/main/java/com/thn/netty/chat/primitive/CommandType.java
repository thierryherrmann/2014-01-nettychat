package com.thn.netty.chat.primitive;

/**
 * Command type. Used (among other things) to find the command's encoder/decoder.
 * @author Thierry Herrmann
 */
public enum CommandType {
    CREATE_ACCOUNT((byte)getIdx()),
    CHANGE_PASSWORD((byte)getIdx()),
    LOGIN((byte)getIdx()),
    LOGOUT((byte)getIdx()),
    EXIT((byte)getIdx()),
    SEND_MSG((byte)getIdx()),
    ADD_CONTACT_INVITE((byte)getIdx()),
    ADD_CONTACT_RESPONSE((byte)getIdx()),
    REMOVE_CONTACT((byte)getIdx()),
    GET_CONTACT_OF_USERS((byte)getIdx()),
    GET_CONTACT_OF_USERS_RESPONSE((byte)getIdx()),
    MESSAGE((byte)getIdx()),
    GET_PENDING_MESSAGES((byte)getIdx()),
    GET_PENDING_MESSAGES_RESPONSE((byte)getIdx()),
    SHUTDOWN_SERVER((byte)getIdx()),
    OK((byte)getIdx()),
    ERROR((byte)getIdx())
    ; 
    static int sIndex;
    private final byte mId;
    static int getIdx(){
        return sIndex++;
    }
    CommandType(byte aId) {
        mId = aId;
    }
    public byte id() {
        return mId;
    }
    public static CommandType forId(byte aId) {
        for (CommandType type : CommandType.values()) {
            if (type.mId == aId) {
                return type;
            }
        }
        throw new IllegalArgumentException("bad id: " + ((int) aId));
    }
    @Override
    public String toString() {
        return super.toString() + "(" + (int) mId + ")";
    }
}