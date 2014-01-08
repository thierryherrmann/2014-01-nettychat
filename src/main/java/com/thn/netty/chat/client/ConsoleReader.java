package com.thn.netty.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.thn.netty.chat.primitive.AddContactInviteCmd;
import com.thn.netty.chat.primitive.AddContactResponseCmd;
import com.thn.netty.chat.primitive.ChatMessageCmd;
import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.CreateAccountRequest;
import com.thn.netty.chat.primitive.ExitRequest;
import com.thn.netty.chat.primitive.GetContactOfUsers;
import com.thn.netty.chat.primitive.GetPendingMessages;
import com.thn.netty.chat.primitive.LoginRequest;
import com.thn.netty.chat.primitive.LogoutRequest;
import com.thn.netty.chat.primitive.MessageInfo;
import com.thn.netty.chat.primitive.RemoveContactCmd;
import com.thn.netty.chat.primitive.ShutdownServerRequest;
import com.thn.netty.chat.primitive.UserName;
import com.thn.netty.chat.user.ContactState;
import com.thn.netty.chat.util.IdGenerator;

/**
 * {@link CommandReader} implementation obtaining commands from the console.
 * @author Thierry Herrmann
 */
public class ConsoleReader implements CommandReader {
    private final IdGenerator mIdGen;
    private List<CommandFactory> mFactories = new ArrayList<>();
    
    /**
     * {@link ConsoleReader} constructor.
     * @param aIdGen ID generator to be used to generate commands.
     */
    public ConsoleReader(IdGenerator aIdGen) {
        mIdGen = aIdGen;
        setupFactories();
    }

    @Override
    public Command getCommand() throws BadCommandException {
        System.out.println("Enter <command#> <arg1> <arg2> ...: ");
        for (int i = 0; i < mFactories.size(); i++) {
            System.out.println(new StringBuilder().append(i).append(") ").append(mFactories.get(i).getPrompt()));
        }
        List<String> line = readCommandLine();
        if (line.isEmpty()) {
            return null; // end of command stream
        }
        String cmdIndexStr = line.remove(0);
        try {
            int cmdIndex = Integer.parseInt(cmdIndexStr);
            if (cmdIndex < 0 || cmdIndex >= mFactories.size()) {
                throw new BadCommandException("invalid command#. Must be from 0 to " + (mFactories.size()-1));
            }
            return mFactories.get(cmdIndex).getCommand(line.toArray(new String[0]));
        } catch (NumberFormatException e) {
            throw new BadCommandException("invalid command#. Must be from 0 to " + (mFactories.size()-1));
        }
    }
    
    private void setupFactories() {
        mFactories.add(new CommandFactory("Exit", new String[0]) {
            @Override public Command createCommand(String[] aArgs) {
                return new ExitRequest(mIdGen.nextId());
            }
        });
        mFactories.add(new CommandFactory("Create Account", new String[] {"username", "password"}) {
            @Override public Command createCommand(String[] aArgs) {
                return new CreateAccountRequest(mIdGen.nextId(), new UserName(aArgs[0]), aArgs[1]);
            }
        });
        mFactories.add(new CommandFactory("Login", new String[] {"username", "password"}) {
            @Override public Command createCommand(String[] aArgs) {
                return new LoginRequest(mIdGen.nextId(), new UserName(aArgs[0]), aArgs[1]);
            }
        });
        mFactories.add(new CommandFactory("Logout", new String[0]) {
            @Override public Command createCommand(String[] aArgs) {
                return new LogoutRequest(mIdGen.nextId());
            }
        });
        mFactories.add(new CommandFactory("Add Contact Invite", new String[] {"contactName"}) {
            @Override public Command createCommand(String[] aArgs) {
                return new AddContactInviteCmd(mIdGen.nextId(), null, new UserName(aArgs[0]));
            }
        });
        mFactories.add(new CommandFactory("Add Contact Response", new String[] {"userName", "accepted(true/false)"}) {
            @Override public Command createCommand(String[] aArgs) {
                return new AddContactResponseCmd(mIdGen.nextId(), new UserName(aArgs[0]), null,
                                                 Boolean.parseBoolean(aArgs[1]));
            }
        });
        mFactories.add(new CommandFactory("Remove Contact", new String[] {"contactName"}) {
            @Override public Command createCommand(String[] aArgs) {
                return new RemoveContactCmd(mIdGen.nextId(), new UserName(aArgs[0]));
            }
        });
        mFactories.add(new CommandFactory("Get Pending Contact Invitations", new String[0]) {
            @Override public Command createCommand(String[] aArgs) {
                return new GetContactOfUsers(mIdGen.nextId(), ContactState.PENDING);
            }
        });
        mFactories.add(new CommandFactory("Instant Message", new String[] {"recipient", "message"}) {
            @Override public Command createCommand(String[] aArgs) {
                return new ChatMessageCmd(mIdGen.nextId(), new MessageInfo(null, new UserName(aArgs[0]), aArgs[1]));
            }
        });
        mFactories.add(new CommandFactory("Get Pending Messages", new String[0]) {
            @Override public Command createCommand(String[] aArgs) {
                return new GetPendingMessages(mIdGen.nextId());
            }
        });
        mFactories.add(new CommandFactory("Shutdown Server", new String[] {}) {
            @Override public Command createCommand(String[] aArgs) {
                return new ShutdownServerRequest(mIdGen.nextId());
            }
        });
    }

    private List<String> readCommandLine() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            String line = br.readLine();
            if (line == null) {
                return null;
            }
            String[] args = line.split("\\s");
            List<String> list = new LinkedList<String>();
            for (String str : args) {
                if (str != null && ! str.isEmpty()) {
                    list.add(str);
                }
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException(); // shouldn't happen with System.in
        }
    }
}
