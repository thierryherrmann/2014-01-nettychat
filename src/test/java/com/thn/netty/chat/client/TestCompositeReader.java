package com.thn.netty.chat.client;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.thn.netty.chat.primitive.Command;

/**
 * Unit test for simple App.
 */
@RunWith(MockitoJUnitRunner.class)  
public class TestCompositeReader
{
    @Test
    public void testCommandReadersAddedOneByOne() throws Exception
    {
        Command cmd1 = mock(Command.class); 
        Command cmd2 = mock(Command.class); 
        CommandReader cmdReader1 = mock(CommandReader.class);
        CommandReader cmdReader2 = mock(CommandReader.class);
        when(cmdReader1.getCommand()).thenReturn(cmd1, (Command) null);
        when(cmdReader2.getCommand()).thenReturn(cmd2, (Command) null);
        CompositeReader reader = new CompositeReader();
        assertNull(reader.getCommand());
        reader.addReader(cmdReader1);
        assertSame(cmd1, reader.getCommand());
        assertNull(reader.getCommand());
        reader.addReader(cmdReader2);
        assertSame(cmd2, reader.getCommand());
        assertNull(reader.getCommand());
    }
    @Test
    public void testCommandReadersAddedInConstructor() throws Exception
    {
        Command cmd1 = mock(Command.class); 
        Command cmd2 = mock(Command.class); 
        CommandReader cmdReader1 = mock(CommandReader.class);
        CommandReader cmdReader2 = mock(CommandReader.class);
        when(cmdReader1.getCommand()).thenReturn(cmd1, (Command) null);
        when(cmdReader2.getCommand()).thenReturn(cmd2, (Command) null);
        CompositeReader reader = new CompositeReader(cmdReader1, cmdReader2);
        assertSame(cmd1, reader.getCommand());
        assertSame(cmd2, reader.getCommand());
        assertNull(reader.getCommand());
    }
}
