package uc3m.apptel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import uc3m.apptel.utils.ChatInfoContainer;
import uc3m.apptel.utils.ChatListAdapter;
import uc3m.apptel.utils.ChatListItem;
import uc3m.apptel.utils.EnumCommand;
import uc3m.apptel.utils.EnumPayload;
import uc3m.apptel.utils.Message;
import uc3m.apptel.utils.UserInfo;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class Client extends Thread {
	private static final String TAG = "HablamosClient";
	private static final int PORT = 8888;
	private static final String IP = "5.231.82.25";
	static CopyOnWriteArrayList<Message> msgsToSend = new CopyOnWriteArrayList<Message>();
	private UserInfo uInfo = null;
	private Activity mainAct;
	private int userId;

	public Client(Activity mainAct, int userId) {
		this.mainAct = mainAct;
		this.userId = userId;
	}
	
	private void runOnUI(ChatInfoContainer auxTab, Message msg, boolean addMessage) {
		int indTab = ChatActivity.chatInfos.indexOf(auxTab);

		if (indTab < 0) {
			mainAct.runOnUiThread(new ChatTabAdder(msg.getOrigId()));
			while(!ChatActivity.chatInfos.contains(auxTab));
			indTab = ChatActivity.chatInfos.indexOf(auxTab);
		}
		if (addMessage) {
			while(ChatActivity.chatInfos.get(indTab).getListAdapter() == null);
			mainAct.runOnUiThread(new ChatMessageAdder(ChatActivity.chatInfos.get(indTab).getListAdapter(), msg));
		} else {
			mainAct.runOnUiThread(new ChatTickAdder(ChatActivity.chatInfos.get(indTab), msg.getMsgId()));
		}
	}

	@Override
	public void run() {
		Message msg = null;
		ChatInfoContainer auxTab = new ChatInfoContainer(0);

		Log.i(TAG, "Conectando con el servidor...");
		try {
			uInfo = new UserInfo(userId, SocketChannel.open(new InetSocketAddress(InetAddress.getByName(IP), PORT)));
			uInfo.getSock().configureBlocking(false);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return;
		}

		Log.i(TAG, "Conectado! " + uInfo.getSock().socket());
		new ClientWriter(uInfo.getSock()).start();
		msgsToSend.add(new Message(EnumCommand.CMD_REGISTER, Message.VERSION, Message.HEADER_LEN, userId, 0, 0, EnumPayload.PAYLOAD_EMPTY, null));
		Log.i(TAG, "Mensaje de registro enviado!");

		while (true) {
			try {
				uInfo.getSock().read(uInfo.getBuf());
				while ((msg = uInfo.retrieveMsg()) != null) {
					System.out.println("Message received: " + msg);
					auxTab.setDestId(msg.getOrigId()); // Tab representing conversation with user who sent you the message

					switch (msg.getCmd()) {
					case CMD_SEND:
						Log.i(TAG, "Mensaje recibido!! :)");
						runOnUI(auxTab, msg, true);
						msgsToSend.add(new Message(EnumCommand.SUCC_ANSWER_TO_SEND, Message.VERSION, Message.HEADER_LEN, msg.getDestId(), msg.getOrigId(), msg.getMsgId(), EnumPayload.PAYLOAD_EMPTY, null));
						break;
					case SUCC_ANSWER_TO_SEND:
						Log.i(TAG, "Primer tick");
						runOnUI(auxTab, msg, false);
						break;
					case ERR_ANSWER_TO_SEND:
						Log.i(TAG, "Error en el primer tick. Retrying");
						// Retry ()
						break;
					case CMD_DELIVERED:
						Log.i(TAG, "Segundo tick");
						runOnUI(auxTab, msg, false);
						msgsToSend.add(new Message(EnumCommand.SUCC_ANSWER_TO_DELIVERED, Message.VERSION, Message.HEADER_LEN, msg.getDestId(), msg.getOrigId(), msg.getMsgId(), EnumPayload.PAYLOAD_EMPTY, null));
						break;
					case SUCC_ANSWER_TO_REGISTER:
						Log.i(TAG, "Conectado correctamente");
						Intent intent = new Intent(mainAct, ChatActivity.class); // UserListActivity.class
						intent.putExtra(ChatActivity.ARG_USER_ID, userId);
						mainAct.startActivity(intent);
						Client.sleep(10);
						break;
					case ERR_ANSWER_TO_REGISTER:
						Log.w(TAG, "Error en register");
						break;
					case SUCC_ANSWER_TO_UNREGISTER:
						Log.i(TAG, "Desconectado correctamente");
						break;
					case ERR_ANSWER_TO_UNREGISTER:
						// Posible reintento hasta recibir SUCC_...
						break;
					case CMD_REGISTER:
					case CMD_UNREGISTER:
					case SUCC_ANSWER_TO_DELIVERED:
					case ERR_ANSWER_TO_DELIVERED:
					default: // Server is not supposed to receive those
						break;
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "Error! " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private class ChatTabAdder implements Runnable {
		private int id;

		public ChatTabAdder(int id) {
			this.id = id;
		}

		@Override
		public void run() {
			ChatActivity.chatInfos.add(new ChatInfoContainer(id));
			ChatActivity.tabsAdapter.notifyDataSetChanged();
		}

	}

	private class ChatMessageAdder implements Runnable {
		private ChatListAdapter lstAdapter;
		private Message msg;

		public ChatMessageAdder(ChatListAdapter lstAdapter, Message msg) {
			this.lstAdapter = lstAdapter;
			this.msg = msg;
		}

		@Override
		public void run() {
			lstAdapter.add(new ChatListItem(false, msg.getMsgId(), new String(msg.getPayload())));
			lstAdapter.notifyDataSetChanged();
		}

	}

	private class ChatTickAdder implements Runnable {
		private ChatInfoContainer chatInfo;
		private int msgId;

		public ChatTickAdder(ChatInfoContainer chatInfo, int msgId) {
			this.chatInfo = chatInfo;
			this.msgId = msgId;
		}

		@Override
		public void run() {
			int pos = chatInfo.getMsgs().indexOf(new ChatListItem(false, msgId, ""));

			if (pos > -1) {
				if (chatInfo.getMsgs().get(pos).hasTick1()) {
					chatInfo.getMsgs().get(pos).setTick2();
				} else {
					chatInfo.getMsgs().get(pos).setTick1();
				}
			} else {Log.w(TAG,"Received tick but couldn't find message with mId " + msgId);}
			chatInfo.getListAdapter().notifyDataSetChanged();
		}

	}

	public class ClientWriter extends Thread {
		SocketChannel sock = null;

		public ClientWriter(SocketChannel sock) {
			this.sock = sock;
		}

		private boolean sendMsg(Message msg) {
			try {
				ByteBuffer buf = ByteBuffer.wrap(msg.getBytes());
				while (buf.hasRemaining()) {
					sock.write(buf);
				}
				System.out.println("Message sent: " + msg);
			} catch (Exception e) {
				Log.e(TAG, "Unable to send the following message. " + e.getMessage());
				msgsToSend.add(msg);
				System.out.println("Message intended to be sent: " + msg);
				return false;
			}

			return true;
		}

		@Override
		public void run() {
			while (true) {
				Iterator<Message> it = msgsToSend.iterator();
				while (it.hasNext()) {
					sendMsg(msgsToSend.remove(msgsToSend.indexOf(it.next())));
				}
			}
		}
	}

}
