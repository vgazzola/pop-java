package popjava.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import popjava.base.MessageHeader;
import popjava.util.LogWriter;

/**
 * This class is a RAW implementation of the buffer abstract class
 */
public class BufferRaw extends POPBuffer {
	/**
	 * Size of the buffer
	 */
	public static final int BUFFER_LENGTH = 20000;
	
	/**
	 * Byte buffer to store data
	 */
	protected ByteBuffer buffer;

	/**
	 * Default constructor
	 */
	public BufferRaw() {
	    this(new MessageHeader());
	}

	/**
	 * Constructor with given values
	 * @param messageHeader	Message header to be associated with this buffer
	 */
	public BufferRaw(MessageHeader messageHeader) {
		super(messageHeader);
		buffer = ByteBuffer.allocate(BUFFER_LENGTH);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(MessageHeader.HEADER_LENGTH);
		size = MessageHeader.HEADER_LENGTH;
	}

	@Override
	public byte[] array() {
		return buffer.array();
	}

	@Override
	public MessageHeader extractHeader() {
		messageHeader = new MessageHeader();
		
		if (buffer.limit() >= MessageHeader.HEADER_LENGTH) {
			int requestType = getInt(4);
			messageHeader.setRequestType(requestType);
			
			switch (requestType) {
			case MessageHeader.REQUEST:
				messageHeader.setClassId(getInt(8));
				messageHeader.setMethodId(getInt(12));
				messageHeader.setSenmatics(getInt(16));
				break;
			case MessageHeader.EXCEPTION:
				messageHeader.setExceptionCode(getInt(8));
				break;
			case MessageHeader.RESPONSE:
				break;
			default:
				break;
			}
			position(MessageHeader.HEADER_LENGTH);
		}
		return this.messageHeader;
	}


	@Override
	public boolean getBoolean() {
		final int value = getInt();
		
		if (value == 0){
		    return false;
		}else if(value == 1){
		    return true;
		}
		
		LogWriter.writeDebugInfo("Decoding boolean using wrong buffer type "+this.getClass().getName());
		return value != 0;
		//throw new RuntimeException("Invalid Boolean encoding: "+value);
	}

	@Override
	public float getFloat() {
		return buffer.getFloat();
	}

	@Override
	public int getInt() {
	    final int value = buffer.getInt();
		return value;
	}

	/**
	 * Get int value at the specified index
	 * @param index	index of the value
	 * @return	the int value
	 */
	public int getInt(int index) {
		return buffer.getInt(index);
	}

	@Override
	public char getChar() {
		final char c = buffer.getChar();
		position(position() + 2);
		return c;
	}

	@Override
	public double getDouble() {
		return buffer.getDouble();
	}

	@Override
	public long getLong() {
		return buffer.getLong();
	}

	@Override
	public String getString() {
		int stringLength = getInt();
		if(stringLength < 0){
		    throw new RuntimeException("Invalid string length: "+stringLength);
		}
		
		if(stringLength == 0){
		    return "";
		}
		
		try {
			byte[] data = new byte[stringLength - 1];
			buffer.get(data, 0, data.length);
			
			int padding = 1;
			
			if ((stringLength % 4) != 0){
			    padding += 4 - (stringLength % 4);
			}
			
			position(position() + padding);
			
			return new String(data);
		} catch (Exception e) {
			return "";
		}
	}
	
	private String getStringLength(int length){
		try {
			byte[] data = new byte[length];
			buffer.get(data, 0, length);
			if ((length % 4) != 0){
				this.position(this.position() + 4 - (length % 4));
			}
			return (new String(data)).trim();
		} catch (Exception e) {			
			return "";
		}
	}

	@Override
	public void put(byte value) {
		resize(4);
		buffer.put(value);
		buffer.put((byte) 0);
		buffer.put((byte) 0);
		buffer.put((byte) 0);
	}

	@Override
	public void put(byte[] data) {
		int len = data.length;
		if((len%4) != 0){
			len = len + 4 - len % 4;
		}
		resize(len);
		buffer.put(data);
		if((data.length % 4) != 0){
			position(position() + 4 - data.length % 4);
		}
	}

	@Override
	public void put(byte[] data, int offset, int length) {
		resize(length);
		buffer.put(data, offset, length);
	}

	@Override
	//TODO: This should really not be an INT, check with popc++ for compatiblity and reduce to one byte
	public void putBoolean(boolean value) {
		if (value == true) {
			putInt(1);
		}else {
			putInt(0);
		}
	}

	@Override
	public void putChar(char value) {
		resize(4);
		buffer.putChar(value);
		buffer.put((byte) 0);
		buffer.put((byte) 0);
	}

	@Override
	public void putFloat(float value) {
		resize(Float.SIZE / Byte.SIZE);
		buffer.putFloat(value);
	}

	@Override
	public void putInt(int value) {
		resize(Integer.SIZE / Byte.SIZE);
		buffer.putInt(value);
	}
	
	/**
	 * Insert int value at a specified index in the buffer
	 * @param index	index to put the value
	 * @param value	the int value to be inserted
	 */
	public void putInt(int index, int value) {
		resize(index, Integer.SIZE / Byte.SIZE);
		buffer.putInt(index, value);
	}

	@Override
	public void putDouble(double value) {
		resize(Double.SIZE / Byte.SIZE);
		buffer.putDouble(value);
	}

	@Override
	public void putLong(long value) {
		resize(Long.SIZE / Byte.SIZE);
		buffer.putLong(value);
	}
	
	/**
     * http://www.javacodegeeks.com/2010/11/java-best-practices-char-to-byte-and.html
     * Around 30% faster than String.getBytes()
     * @param str
     * @return
     */
    private static byte[] stringToBytesASCII(String str) {
    	final byte[] b = new byte[str.length()];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) str.charAt(i);
            if(b[i] == 0){
                throw new RuntimeException("Can no have 0 in a string");
            }
        }
        return b;
    }

	@Override
	public void putString(String data) {
		
		if (data != null && data.length() > 0) {
			int stringLength = data.length() + 1; //0 terminated
			byte[] datas = stringToBytesASCII(data);
			
			int padding = 0;
            
            if ((stringLength % 4) != 0){
                padding = 4 - (stringLength % 4);
            }
			
			//Integrate putInt code so that resize is called only once
			resize(stringLength + (Integer.SIZE / Byte.SIZE) + padding);
			buffer.putInt(stringLength);
			buffer.put(datas);
			//buffer.put((byte) 0);//0 terminated
			
			for(int i = 0; i < padding + 1; i++){//0 terminated + padding (also 0)
			    buffer.put((byte) 0);
			}
			
			//position(position() + padding);
		} else {
			putInt(0);
		}
	}

	@Override
	public void reset() {
		buffer.clear();
		size = MessageHeader.HEADER_LENGTH;
		for(int i = 0; i < size; i++){
		    buffer.put((byte) 0);
		}
		//position(size);
	}

	@Override
	public void resetToReceive() {
		buffer.clear();
		size = 0;
		position(size);
	}

	@Override
	//TODO: remove this from this class
	public int getTranslatedInteger(byte[] value) {
		return (value[3] & 0xff) << 24 | (value[2] & 0xff) << 16 | (value[1] & 0xff) << 8 | (value[0] & 0xff);		
	}

	/**
	 * 
	 */
	@Override
    public String toIntString() {
		int position = this.position();
		this.position(0);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size() / 4; i++) {
			sb.append(this.getInt());
			sb.append('_');
		}
		this.position(position);
		return sb.toString();
	}

	/**
	 * 
	 */
	@Override
    public String toCharString() {
		int position = this.position();
		this.position(0);
		StringBuilder sb = new StringBuilder();
		sb.append("Data in bytes:");
		for (int i = 0; i < size(); i++) {
			byte data = buffer.get();
			int byteValue = new Integer(data).intValue();
			if (byteValue < 0) {
				byteValue = data & 0x80;
				byteValue += data & 0x7F;
			}
			sb.append(byteValue);
			sb.append('_');
		}
		this.position(position);
		return sb.toString();
	}

	/**
	 * Get the current buffer's position
	 * @return	the buffer's position
	 */
	public int position() {
		return buffer.position();
	}

	/**
	 * Set the pointer to the index
	 * @param index	index to set the pointer
	 */
	public void position(int index) {
		if (index > size){
			size = index;
		}
		buffer.position(index);
	}

	/**
	 * Return the buffer's limit
	 * @return the limit of this buffer
	 */
	protected int limit() {
		return buffer.limit();
	}

	private void resizeBuffer(int newCapacity){
		ByteBuffer tempBuffer = ByteBuffer.allocate(newCapacity);
		tempBuffer.order(buffer.order());
		tempBuffer.put(buffer.array(), 0 , buffer.position());
		buffer = tempBuffer;
	}
	
	/**
	 * Resize the current buffer to store more data
	 * @param moreCapacity	The additional capacity to add on the current buffer
	 */
	public void resize(int moreCapacity) {
		size += moreCapacity;
		int position = position() + moreCapacity;
		if (position >= buffer.capacity()) {
			int newCapacity = position * 2;
			resizeBuffer(newCapacity);
		}
	}

	public void resize(int position, int moreCapacity) {
		position = position + moreCapacity;
		if (position > size){
			size = position;
		}
		int capacity = buffer.capacity();
		if (position > capacity / 2) {
			int newCapacity = position * 2;
			ByteBuffer tempBuffer = ByteBuffer.allocate(newCapacity);
			tempBuffer.order(buffer.order());
			tempBuffer.put(buffer);
			buffer = tempBuffer;
		}
	}

	

	@Override
	public void putBooleanArray(boolean[] value) {
		int[] transfert = new int[value.length];
		for (int i = 0; i < value.length; i++) {
			if(value[i])
				transfert[i]=1;
			else 
				transfert[i]=0;
		}
		this.putIntArray(transfert);
	}


	@Override
	public void putDoubleArray(double[] value) {
		int arrayLength = 0;
		if (value != null)
			arrayLength = value.length;
		this.putInt(arrayLength);
		if(arrayLength>0)
		{
			this.resize(arrayLength*Double.SIZE/Byte.SIZE);
			DoubleBuffer doubleBuffer=buffer.asDoubleBuffer();
			doubleBuffer.put(value);
			this.position(this.position()+arrayLength*Double.SIZE/Byte.SIZE);			
		}
	}

	@Override
	public void putFloatArray(float[] value) {
		int arrayLength = 0;
		if (value != null){
			arrayLength = value.length;
		}
		
		LogWriter.writeDebugInfo("STORE FLOAT "+arrayLength);
		
		this.putInt(arrayLength);
		if(arrayLength > 0)
		{
			resize(arrayLength * Float.SIZE/Byte.SIZE);
			FloatBuffer floatBuffer = buffer.asFloatBuffer();
			floatBuffer.put(value);
			position(position() + arrayLength*Float.SIZE/Byte.SIZE);
		}
	}

	@Override
	public void putIntArray(int[] value) {
		int arrayLength = 0;
		
		if (value != null){
			arrayLength = value.length;
		}
		
		putInt(arrayLength);
		
		if(arrayLength > 0) {
			resize(arrayLength *Integer.SIZE/Byte.SIZE);
			IntBuffer intBuffer = buffer.asIntBuffer();
			intBuffer.put(value);
			position(position()+arrayLength * Integer.SIZE/Byte.SIZE);			
		}		
	}

	@Override
	public void putLongArray(long[] value) {
		int arrayLength = 0;
		if (value != null)
			arrayLength = value.length;
		this.putInt(arrayLength);
		if(arrayLength>0)
		{
			this.resize(arrayLength*Long.SIZE/Byte.SIZE);
			LongBuffer longBuffer=buffer.asLongBuffer();
			longBuffer.put(value);
			this.position(this.position()+arrayLength*Long.SIZE/Byte.SIZE);
		}		
	}


	@Override
	public byte get() {
		return buffer.get();
	}

	@Override
	public boolean[] getBooleanArray(int length) {
		int[] transfert = this.getIntArray(length);
		boolean[] ret = new boolean[transfert.length];
		for (int i = 0; i < transfert.length; i++) {
			if(transfert[i]==1)
				ret[i] = true;
			else
				ret[i] = false;
		}
		return ret;
	}

	@Override
	public byte[] getByteArray(int length) {
		byte [] result = new byte[length];
		if(length > 0){
		    buffer.get(result);
	        
	        if((length % 4) != 0){
	            buffer.position(position() + 4 - length%4);
	        }
		}
		
		return result;
	}

	@Override
	public double[] getDoubleArray(int length) {
	    double [] result = new double[length];
	    
	    if(length > 0){
	        DoubleBuffer doubleBuffer = buffer.asDoubleBuffer();
	        doubleBuffer.get(result);       
	        position(position()+length*Double.SIZE/Byte.SIZE);
	    }
	    
		return result;
	}

	@Override
	public float[] getFloatArray(int length) {
		float [] result = new float[length];
		
		if(length > 0){
		    FloatBuffer floatBuffer=buffer.asFloatBuffer();
	        floatBuffer.get(result);
	        position(position()+length*Float.SIZE/Byte.SIZE);
		}
		
		return result;
	}

	@Override
	public int[] getIntArray(int length) {
		int [] result = new int[length];
		
		if(length > 0){
		    IntBuffer intBuffer = buffer.asIntBuffer();
	        intBuffer.get(result);
	        position(position()+ length*Integer.SIZE/Byte.SIZE);
		}
		
		return result;
	}

	@Override
	public long[] getLongArray(int length) {
		long[]result = new long[length];
		if(length > 0){
		    LongBuffer longBuffer=buffer.asLongBuffer();
	        longBuffer.get(result);
	        position(position()+length*Long.SIZE/Byte.SIZE);
		}
		
		return result;
	}

	@Override
	public void putByteArray(byte[] value) {
	    if(value != null){
	        putInt(value.length);
	        put(value);
	    }else{
	        putInt(0);
	    }
	}
	
	/**
	 * Format:
	 * Size
	 * Type (0, 1, 2) (response, request, exeption)
	 * 
	 * 
	 * @return
	 */
	@Override
	public int packMessageHeader() {
		int index = 0;
		for (index = 0; index < 5; index++) {
			putInt(index * 4, 0); //0, 4, 8, 12
		}
		int type = messageHeader.getRequestType();
		putInt(0, size());
		
		putInt(4, type);
		switch (type) {
			case MessageHeader.REQUEST:
				putInt(8, messageHeader.getClassId());
				putInt(12, messageHeader.getMethodId());
				putInt(16, messageHeader.getSenmatics());
				break;
			case MessageHeader.EXCEPTION:
				putInt(8, messageHeader.getExceptionCode());
				break;
			case MessageHeader.RESPONSE:
				break;
			default:
				break;
		}
		return 0;
	}

	@Override
	public short getShort() {
		return buffer.getShort();
	}

	@Override
	public void putShort(short value) {
		resize(Short.SIZE / Byte.SIZE);
		buffer.putShort(value);
	}

	@Override
	public short[] getShortArray(int length) {
		short [] result = new short[length];
		ShortBuffer shortBuffer = buffer.asShortBuffer();
		shortBuffer.get(result);
		position(position()+length*Short.SIZE/Byte.SIZE);
		return result;
	}

	@Override
	public void putShortArray(short[] value) {
		int arrayLength = 0;
		if (value != null){
			arrayLength = value.length;
		}
		
		putInt(arrayLength);
		if(arrayLength>0){
			resize(arrayLength*Short.SIZE/Byte.SIZE);
			ShortBuffer shortBuffer=buffer.asShortBuffer();
			shortBuffer.put(value);
			position(this.position()+arrayLength*Short.SIZE/Byte.SIZE);			
		}
		
	}

	@Override
	public char[] getCharArray(int length) {
		String s = getStringLength(length);
		char[] ret = s.toCharArray();
		return ret;
	}

	@Override
	public void putCharArray(char[] value) {
		String arrayAsString = new String(value);
		putString(arrayAsString);
	}
}
