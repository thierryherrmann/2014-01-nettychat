package com.thn.netty.chat.primitive;

/**
 * Invite request to add a contact.</p>
 * <code>
Messages exchanged to add a contact:</br>

addContactInvite (null, contactName) (req) --&gt; </br>
&nbsp;&nbsp;                --&gt; addContactInvite (contactId, null) (notif) </br>
&nbsp;&nbsp;                ok &lt;-- (notif resp)</br>
ok &lt;-- (resp)
</br></br>
&nbsp;&nbsp;                addContactResponse (req) &lt;-- (if accept, add contact to DB/memory)</br>
addContactResponse (notif) &lt;--</br>
ok (notif resp) --&gt;</br>
&nbsp;&nbsp;                ok --&gt;  (resp)</br>
 * </code>
 * @author Thierry Herrmann
 */
public class AddContactInviteCmd extends BaseAddContactCmd {

    /**
     * Invite request to add a contact.
     * @param aCmdId command id.
     * @param aUserName name of the requester user. Null for the current user.
     * @param aContactName name of the new contact. Null for the current user.  
     */
    public AddContactInviteCmd(int aCmdId, UserName aUserName, UserName aContactName) {
        super(CommandType.ADD_CONTACT_INVITE, aCmdId, aUserName, aContactName);
    }
}
