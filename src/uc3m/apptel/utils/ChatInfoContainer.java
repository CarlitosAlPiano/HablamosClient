package uc3m.apptel.utils;

import java.util.ArrayList;

import uc3m.apptel.ChatActivity.ChatFragment;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatInfoContainer implements Parcelable {
	private int destId;
	private ArrayList<ChatListItem> msgs;
	private ChatListAdapter lstAdapter = null;
	private int msgId;

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

	public ChatListAdapter getListAdapter() {
		return lstAdapter;
	}

	public ArrayList<ChatListItem> getMsgs() {
		return msgs;
	}

	public void setDestId(int destId) {
		this.destId = destId;
	}

	public void setListAdapter(ChatListAdapter lstAdapter) {
		this.lstAdapter = lstAdapter;
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

	public int getMsgId() {
		return msgId;
	}

	public void incMsgId() {
		this.msgId++;
	}

}
