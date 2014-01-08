package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.thn.netty.chat.util.Util;

/**
 * Generic record implementation contaning fields of various types to help encoding/decoding commands.
 * @author Thierry Herrmann
 */
public class Record {
    private boolean mActive = true;
    private List<Field> mFields = new LinkedList<>();
    private Iterator<Field> mFieldIterator;
    
    private Record() {
        // force use of static method
    }
    
    private static enum Type {
        BOOLEAN((byte)getIdx()),
        BYTE((byte)getIdx()),
        INT((byte)getIdx()),
        LONG((byte)getIdx()),
        STRING((byte)getIdx()),
        ; 
        static int sIndex;
        private final byte mCode;
        static int getIdx(){
            return sIndex++;
        }
        private Type(byte aCode) {
            mCode = aCode;
        }
    }
    private static class Field {
        private Type mType;
        private Object mValue;
        private Field(Type aType, Object aValue) {
            mType = aType;
            mValue = aValue;
        }
        private void write(ByteBuf aOut) {
            switch (mType) {
            case BOOLEAN:
                aOut.writeByte(Type.BOOLEAN.mCode);
                aOut.writeBoolean((Boolean) mValue);
                break;
            case BYTE:
                aOut.writeByte(Type.BYTE.mCode);
                aOut.writeByte((Byte) mValue);
                break;
            case INT:
                aOut.writeByte(Type.INT.mCode);
                ByteBuf intValue = Unpooled.copyInt((Integer)mValue);
                aOut.writeBytes(intValue);
                break;
            case LONG:
                aOut.writeByte(Type.LONG.mCode);
                ByteBuf longValue = Unpooled.copyLong((Long)mValue);
                aOut.writeBytes(longValue);
                break;
            case STRING:
                aOut.writeByte(Type.STRING.mCode);
                if (mValue == null) {
                    aOut.writeInt(-1); // for null string, as opposed to 0 for empty string 
                }
                else {
                    byte[] bytes = ((String)mValue).getBytes(Util.UTF8);
                    ByteBuf value = Unpooled.copiedBuffer(bytes);
                    aOut.writeInt(bytes.length);
                    aOut.writeBytes(value);
                }
                break;
            default:
                throw new IllegalStateException("unknown type: " + mType);
            }
        }
        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("type", mType)
            .append("value", mValue)
            .toString();
        }
    }

    public Record addBoolean(boolean aValue) {
        checkWriteMode();
        mFields.add(new Field(Type.BOOLEAN, aValue));
        return this;
    }
    public Record addByte(byte aValue) {
        checkWriteMode();
        mFields.add(new Field(Type.BYTE, Byte.valueOf(aValue)));
        return this;
    }
    public Record addInt(int aValue) {
        checkWriteMode();
        mFields.add(new Field(Type.INT, Integer.valueOf(aValue)));
        return this;
    }
    public Record addLong(long aValue) {
        checkWriteMode();
        mFields.add(new Field(Type.LONG, Long.valueOf(aValue)));
        return this;
    }
    public Record addString(String aValue) {
        checkWriteMode();
        mFields.add(new Field(Type.STRING, aValue));
        return this;
    }
    
    public void write(ByteBuf aOut) {
        checkWriteMode();
        ByteBuf buffer = Unpooled.buffer();
        for (Field field : mFields) {
            field.write(buffer);
        }
        aOut.writeInt(buffer.writerIndex());
        aOut.writeBytes(buffer);
        mActive = false;
    }
    
    public static Record forWrite() {
        return new Record();
    }
    public static Record read(ByteBuf aIn) {
        Record rec = new Record();
        int length = aIn.readInt();
        int bytesRead = 0;
        while(bytesRead < length) {
            byte type = aIn.readByte();
            bytesRead++;
            if (type == Type.BOOLEAN.mCode) {
                byte value = aIn.readByte();
                bytesRead += 1;
                if (value == 0) {
                    rec.addBoolean(false);
                }
                else if (value == 1) {
                    rec.addBoolean(true);
                }
                else {
                    throw new IllegalStateException("unknown boolean value: " + value);
                }
            }
            else if (type == Type.BYTE.mCode) {
                bytesRead += 1;
                rec.addByte(aIn.readByte());
            }
            else if (type == Type.INT.mCode) {
                bytesRead += 4;
                rec.addInt(aIn.readInt());
            }
            else if (type == Type.LONG.mCode) {
                bytesRead += 8;
                rec.addLong(aIn.readLong());
            }
            else if (type == Type.STRING.mCode) {
                int fieldLength = aIn.readInt();
                if (fieldLength == -1) { // -1 for null string as opposed to 0 for empty string
                    bytesRead += 4;
                    rec.addString(null);
                }
                else {
                    bytesRead += (4 + fieldLength);
                    ByteBuf buffer = Unpooled.buffer(fieldLength, fieldLength);
                    aIn.readBytes(buffer);
                    rec.addString(buffer.toString(Util.UTF8));
                }
            }
            else {
                throw new IllegalStateException("unknown field type: " + type);
            }
        }
        rec.mFieldIterator = rec.mFields.iterator();
        return rec;
    }

    public boolean getBoolean() {
        checkReadMode();
        return ((Boolean) getNext(Type.BOOLEAN)).booleanValue();
    }
    public byte getByte() {
        checkReadMode();
        return ((Byte) getNext(Type.BYTE)).byteValue();
    }
    public int getInt() {
        checkReadMode();
        return ((Integer) getNext(Type.INT)).intValue();
    }
    public int getLong() {
        checkReadMode();
        return ((Long) getNext(Type.LONG)).intValue();
    }
    public String getString() {
        checkReadMode();
        return (String) getNext(Type.STRING);
    }
    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        for (Field field : mFields) {
            builder.append(field.toString());
        }
        return builder.toString();
    }

    private void checkActive() {
        if (!mActive) {
            throw new IllegalStateException("record already used");
        }
    }

    private void checkReadMode() {
        checkActive();
        if (mFieldIterator == null) {
            throw new IllegalStateException("must be in read mode");
        }
    }

    private void checkWriteMode() {
        checkActive();
        if (mFieldIterator != null) {
            throw new IllegalStateException("must be in write mode");
        }
    }

    private Object getNext(Type aType) {
        if (!mFieldIterator.hasNext()) {
            throw new NoSuchElementException();
        }
        Field field = mFieldIterator.next();
        if (field.mType != aType) {
            throw new NoSuchElementException("Type expected: " + aType + "; actual: " + field.mType);
        }
        if (!mFieldIterator.hasNext()) {
            mActive = false;
        }
        return field.mValue;
    }

    public static void main(String[] args) {
        Record rec = Record.forWrite();
        rec.addBoolean(true);
        rec.addInt(42);
        rec.addLong(43);
        rec.addString(null);
        rec.addString("Hello!");
        System.out.println("rec: " + rec);
        
        ByteBuf buffer = Unpooled.buffer();
        rec.write(buffer);
        System.out.println(buffer);
        
        Record readRec = Record.read(buffer);
        System.out.println("read: " + readRec);
        System.out.println(readRec.getBoolean());
        System.out.println(readRec.getInt());
        System.out.println(readRec.getLong());
        System.out.println(readRec.getString());
        System.out.println(readRec.getString());
    }
}
