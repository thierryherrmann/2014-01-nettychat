package com.thn.netty.chat.user;

/**
 * Constact state. 
 * <ul>
 * <li>CONTACT: contact</li>
 * <li>PENDING: the contact request is not yet approved by the add contact invitation recipient</li>
 * <li>BLOCKED: the contact is blocked and can't send IMs to the current user</li>
 * </ul>
 * @author Thierry Herrmann
 */
public enum ContactState {
    PENDING((byte)getIdx()),
    CONTACT((byte)getIdx()),
    BLOCKED((byte)getIdx()),
    ; 
    static int sIndex;
    private final byte mId;
    static int getIdx(){
        return sIndex++;
    }
    ContactState(byte aId) {
        mId = aId;
    }
    public byte id() {
        return mId;
    }
    public static ContactState forId(byte aId) {
        for (ContactState state : ContactState.values()) {
            if (state.mId == aId) {
                return state;
            }
        }
        throw new IllegalArgumentException("bad id: " + ((int) aId));
    }
    @Override
    public String toString() {
        return super.toString() + "(" + (int) mId + ")";
    }
}
