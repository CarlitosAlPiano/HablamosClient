package uc3m.apptel.utils;

import java.util.Arrays;

public class Message {
	private EnumCommand cmd;
	private int version;
	private int length;
	private int origId;
	private int destId;
	private int msgId;
	private EnumPayload pldType;
	private byte[] payload;
	public static final int HEADER_LEN = 20;
	public static final int VERSION = 1;

	public Message(byte[] msg) {
		this(msg, Arrays.copyOfRange(msg, HEADER_LEN, msg.length));
	}

	public Message(byte[] hdr, byte[] payload) {
		this(EnumCommand.fromInt(read2bytes(hdr, 0)), read2bytes(hdr, 2),
				read2bytes(hdr, 4), read4bytes(hdr, 6), read4bytes(hdr, 10),
				read4bytes(hdr, 14), EnumPayload.fromInt(read2bytes(hdr, 18)),
				payload);
	}

	public Message(EnumCommand cmd, int version, int length, int origId,
			int destId, int msgId, EnumPayload pldType, byte[] payload) {
		this.cmd = cmd;
		this.version = version;
		this.length = length;
		this.origId = origId;
		this.destId = destId;
		this.msgId = msgId;
		this.pldType = pldType;
		if (payload == null) {
			this.payload = new byte[0];
		} else {
			this.payload = payload;
		}
	}

	public byte[] getBytes() {
		byte[] res = new byte[HEADER_LEN + payload.length];

		System.arraycopy(write2bytes(cmd.getValue()), 0, res, 0, 2);
		System.arraycopy(write2bytes(version), 0, res, 2, 2);
		System.arraycopy(write2bytes(length), 0, res, 4, 2);
		System.arraycopy(write4bytes(origId), 0, res, 6, 4);
		System.arraycopy(write4bytes(destId), 0, res, 10, 4);
		System.arraycopy(write4bytes(msgId), 0, res, 14, 4);
		System.arraycopy(write2bytes(pldType.getValue()), 0, res, 18, 2);
		System.arraycopy(payload, 0, res, HEADER_LEN, payload.length);

		return res;
	}

	/*private static short read2bytes(byte[] b, int offs) {
		short s = ByteBuffer.wrap(b, offs, 2)
				.order(ByteOrder.BIG_ENDIAN).getShort();
		return s;
	}

	private static int read4bytes(byte[] b, int offs) {
		int s = ByteBuffer.wrap(b, offs, 4).order(ByteOrder.BIG_ENDIAN)
				.getInt();
		return s;
	}

	private static byte[] getByteArrayfromShort(short s) {
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.putShort(s).order(ByteOrder.LITTLE_ENDIAN);
		buffer.flip();
		return buffer.array();
	}

	private static byte[] getByteArrayfromInt(int i) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(i).order(ByteOrder.LITTLE_ENDIAN);
		buffer.flip();
		return buffer.array();
	}*/

	private static int read2bytes(byte b1, byte b2) {
		return ((b1 & 0xFF) << 8) | (b2 & 0xFF);
	}

	private static int read2bytes(byte[] b, int offs) {
		return read2bytes(b[offs], b[offs + 1]);
	}

	private static int read4bytes(byte b1, byte b2, byte b3, byte b4) {
		return ((b1 & 0xFF) << 24) | ((b2 & 0xFF) << 16) | ((b3 & 0xFF) << 8)
				| (b4 & 0xFF);
	}

	private static int read4bytes(byte[] b, int offs) {
		return read4bytes(b[offs], b[offs + 1], b[offs + 2], b[offs + 3]);
	}

	private static byte[] write2bytes(int i) {
		return new byte[] { (byte) ((i >> 8) & 0xFF), (byte) (i & 0xFF) };
	}

	private static byte[] write4bytes(int i) {
		return new byte[] { (byte) ((i >> 24) & 0xFF),
				(byte) ((i >> 16) & 0xFF), (byte) ((i >> 8) & 0xFF),
				(byte) (i & 0xFF) };
	}

	public static int getTotalLength(byte[] header) {
		return read2bytes(header, 4);
	}

	public static int getPayloadLength(byte[] header) {
		return getTotalLength(header) - HEADER_LEN;
	}

	public EnumCommand getCmd() {
		return cmd;
	}

	public int getVersion() {
		return version;
	}

	public int getLength() {
		return length;
	}

	public int getOrigId() {
		return origId;
	}

	public int getDestId() {
		return destId;
	}

	public int getMsgId() {
		return msgId;
	}

	public EnumPayload getPldType() {
		return pldType;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setCmd(EnumCommand cmd) {
		this.cmd = cmd;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setOrigId(int origId) {
		this.origId = origId;
	}

	public void setDestId(int destId) {
		this.destId = destId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public void setPldType(EnumPayload pldType) {
		this.pldType = pldType;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	@Override
	public boolean equals(Object obj) {
		boolean retVal = false;

		if (obj instanceof Message) {
			retVal = (((Message) obj).destId == this.destId);
		}

		return retVal;
	}

	@Override
	public String toString() {
		String str = origId + "->" + destId + "(mId " + msgId + "): " + cmd
				+ "(v" + version + "|Len: " + length + "|Payload: " + pldType
				+ ")";
		if (payload.length > 0) {
			str += "\t[";
			switch (pldType) {
			case PAYLOAD_TEXT:
			case PAYLOAD_HTML:
				str += new String(payload);
				break;
			case PAYLOAD_INT:
				str += read4bytes(payload, 0);
				break;
			case PAYLOAD_PIC_JPEG:
			case PAYLOAD_PIC_PNG:
			case PAYLOAD_FLOAT:
			default:
				str += String.valueOf(payload[0] & 0xFF);
				for (int i = 1; i < payload.length; i++) {
					str += "," + String.valueOf(payload[i] & 0xFF);
				}
				break;
			}
			str += "]";
		}
		return str;
	}
}
