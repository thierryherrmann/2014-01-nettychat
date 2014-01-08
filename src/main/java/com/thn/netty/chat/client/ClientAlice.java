package com.thn.netty.chat.client;

import com.thn.netty.chat.primitive.ExitRequest;
import com.thn.netty.chat.primitive.LoginRequest;
import com.thn.netty.chat.primitive.LogoutRequest;
import com.thn.netty.chat.primitive.UserName;
import com.thn.netty.chat.util.DefaultIdGenerator;
import com.thn.netty.chat.util.IdGenerator;

/**
 * Simple client with pre-recorded commands to login the Alice user before using the text UI.
 * @author Thierry Herrmann
 */
public class ClientAlice {
    public static void main(String[] aArgs) {
        IdGenerator idGen = DefaultIdGenerator.getInstance();
        RecordedCommandReader cmdReader1 = new RecordedCommandReader();
        cmdReader1.add(new LoginRequest(idGen.nextId(), new UserName("Alice"), "mypass"))
                     ;
        ConsoleReader reader2 = new ConsoleReader(idGen);
        RecordedCommandReader reader3 = new RecordedCommandReader();
        reader3.add(new LogoutRequest(idGen.nextId()))
                  .add(new ExitRequest(idGen.nextId()))
                  ;

        new Client(new CompositeReader(cmdReader1, reader2, reader3)).start();
    }
}
