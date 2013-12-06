package uc3m.apptel.utils;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class UserInfo {
	private int id;
	private SocketChannel aSock;
	private ByteBuffer buf = ByteBuffer.allocate(2048);
	private boolean hasMsg = true;
	public static final int MIN_ID = 1001;
	public static final int MAX_ID = 9999;

	public UserInfo(int id) {
		this(id, null);
	}

	public UserInfo(int id, SocketChannel aSock) {
		this.id = id;
		this.aSock = aSock;
	}

	public int getId() {
		return id;
	}

	public SocketChannel getSock() {
		return aSock;
	}

	public ByteBuffer getBuf() {
		return buf;
	}

	public boolean getHasMsg() {
		return hasMsg;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setHasMsg(boolean hasMsg) {
		this.hasMsg = hasMsg;
	}

	public int computePacketLen() {
		int packetLen = -1;
		if (buf.position() >= 6) {
			packetLen = Message.getTotalLength(buf.array());
		}

		return packetLen;
	}

	public Message retrieveMsg() {
		Message msg = null;
		int packetLen = computePacketLen();

		if (buf.position() >= packetLen && packetLen > 0) {
			int pos = buf.position() - packetLen;	// Future buffer position (avoid overwriting chunk of second packet)
			byte[] msgBytes = new byte[packetLen];
			buf.flip();
			buf.get(msgBytes);
			msg = new Message(msgBytes);

			buf.compact();							// Delete message from the buffer
			buf.position(pos);
		}
		
		return msg;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean retVal = false;

        if(obj instanceof UserInfo){
            retVal = (((UserInfo) obj).id == this.id);
        }
        
        return retVal;
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}
}
