package uc3m.apptel.utils;

import java.util.ArrayList;
import java.util.Comparator;

import uc3m.apptel.Client;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;

public class ChatInfoContainer implements Parcelable {
	private int destId;
	private ArrayList<ChatListItem> msgs;
	private ChatListAdapter lstAdapter = null;
	private int msgId;
	private int unreadMsgs;

	// Constructor to call from ChatActivity
	public ChatInfoContainer(int destId) {
		this.destId = destId;
		this.msgs = new ArrayList<ChatListItem>();
		this.msgId = 0;
	}

	// Read the passed-in Parcel and populate object's data from its values.
	// DATA MUST BE READ IN THE EXACT SAME ORDER THAN IT WAS WRITTEN!!
	private ChatInfoContainer(Parcel in) {
		destId = in.readInt();
		in.readList(msgs, null);
	}

	// This is used to regenerate your object. All Parcelables must have a
	// CREATOR that implements these two methods
	public static final Parcelable.Creator<ChatInfoContainer> CREATOR = new Parcelable.Creator<ChatInfoContainer>() {
		public ChatInfoContainer createFromParcel(Parcel in) {
			return new ChatInfoContainer(in);
		}

		public ChatInfoContainer[] newArray(int size) {
			return new ChatInfoContainer[size];
		}
	};

	@Override
	// Write your object's data to the passed-in Parcel
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(destId);
		out.writeList(msgs);
	}

	public int getDestId() {
		return destId;
	}

	public void setDestId(int destId) {
		this.destId = destId;
	}

	public ChatListAdapter getListAdapter() {
		return lstAdapter;
	}

	public ArrayList<ChatListItem> getMsgs() {
		return msgs;
	}

	public void setListAdapter(ChatListAdapter lstAdapter) {
		this.lstAdapter = lstAdapter;
	}

	public int getMsgId() {
		return msgId;
	}

	public void incMsgId() {
		this.msgId++;
	}

	public int getUnread() {
		return unreadMsgs;
	}

	public void incUnread() {
		this.unreadMsgs++;
	}

	public void rstUnread() {
		this.unreadMsgs = 0;
	}

	public Message getLastMsg() {
		return getOriginalMsg(msgs.size() - 1);
	}

	public Message getOriginalMsg(int pos) {
		ChatListItem item = msgs.get(pos);
		if (item.wasSentByUser()) {
			return new Message(EnumCommand.CMD_SEND, Message.VERSION, Message.HEADER_LEN + item.getMsg().length(), Client.getUserId(), destId,
					item.getMsgId(), EnumPayload.PAYLOAD_TEXT, item.getMsg().getBytes());
		} else {
			return new Message(EnumCommand.CMD_SEND, Message.VERSION, Message.HEADER_LEN + item.getMsg().length(), destId, Client.getUserId(),
					item.getMsgId(), EnumPayload.PAYLOAD_TEXT, item.getMsg().getBytes());
		}
	}

	@Override
	// 99.9% of the time you can just ignore this
	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		boolean retVal = false;

		if (obj instanceof ChatInfoContainer) {
			retVal = (((ChatInfoContainer) obj).getDestId() == this.getDestId());
		}

		return retVal;
	}

	public static class Comparador implements Comparator<ChatInfoContainer> {
		@Override
		public int compare(ChatInfoContainer lhs, ChatInfoContainer rhs) {
			ArrayList<ChatListItem> msgsLhs = lhs.getMsgs(), msgsRhs = rhs.getMsgs();
			int numMsgsLhs = msgsLhs.size(), numMsgsRhs = msgsRhs.size();

			if (numMsgsLhs > 0 && numMsgsRhs <= 0) {
				return -1;
			} else if (numMsgsLhs <= 0 && numMsgsRhs <= 0) {
				/*
				 * if (lhs.getDestId() == rhs.getDestId()) return 0;
				 * else return (lhs.getDestId() > rhs.getDestId()) ? 1 : -1;
				 */
				return 0;
			} else if (numMsgsLhs <= 0 && numMsgsRhs > 0) {
				return 1;
			} else {
				return -Time.compare(msgsLhs.get(numMsgsLhs - 1).getTime(), msgsRhs.get(numMsgsRhs - 1).getTime());
			}
		}
	}

}
