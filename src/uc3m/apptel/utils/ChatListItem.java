package uc3m.apptel.utils;

import android.text.format.Time;

public class ChatListItem {
	private Time t = new Time();
	private boolean tick1 = false;
	private boolean tick2 = false;
	private boolean sentByUser;
	private int msgId;
	private String msg;

	public ChatListItem(boolean sentByUser, int msgId, String msg) {
		t.setToNow();
		this.sentByUser = sentByUser;
		this.msgId = msgId;
		this.msg = msg;
	}

	public Time getTime() {
		return t;
	}

	public void setTime(Time t) {
		this.t = t;
	}

	public boolean hasTick1() {
		return tick1;
	}

	public void setTick1() {
		this.tick1 = true;
	}

	public boolean hasTick2() {
		return tick2;
	}

	public void setTick2() {
		this.tick2 = true;
	}

	public boolean wasSentByUser() {
		return sentByUser;
	}

	public int getMsgId() {
		return msgId;
	}

	public String getMsg() {
		return msg;
	}

	/*public void setSentByUser(boolean sentByUser) {
		this.sentByUser = sentByUser;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}*/
	
	@Override
	public boolean equals(Object obj) {
		boolean retVal = false;

		if (obj instanceof ChatListItem) {
			retVal = (((ChatListItem) obj).getMsgId() == this.getMsgId());
		}

		return retVal;
	}

}
