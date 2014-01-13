package uc3m.apptel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import uc3m.apptel.utils.ChatInfoContainer;
import uc3m.apptel.utils.ChatInfoContainer.Comparador;
import uc3m.apptel.utils.ChatListAdapter;
import uc3m.apptel.utils.ChatListItem;
import uc3m.apptel.utils.EnumCommand;
import uc3m.apptel.utils.EnumPayload;
import uc3m.apptel.utils.Message;
import uc3m.apptel.utils.UserInfo;
import uc3m.apptel.utils.UserListAdapter;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;

public class Client extends IntentService {
	public static final String ARG_SHOW_ACT = "show_act";
	public static final boolean DEF_SHOW_ACT = true;
	public static final String ARG_USER_ID = "user_id";
	public static final int DEF_USER_ID = 0;
	public static final int MIN_USER_ID = 1001;
	public static final int MAX_USER_ID = 9999;
	public static final String TAG = "Hablamos";
	public static final long INTERVAL_KEEP_ALIVE_SERVICE = 30000; // 10*60000;
	private static final long INTERVAL_KEEP_ALIVE_SOCKET = 30000; // 5*60000;
	private static final long REGISTER_TIMEOUT = 3000;
	private static final long[] VIB_PATTERN = { 0, 100, 50, 100, 50, 100, 50 };
	private static final long[] VIB_ERROR_PATTERN = { 0, 500 };
	private static final String IP = "5.231.82.25";
	// private static final String IP = "192.168.0.10";
	private static final int PORT = 8888;
	private static final int NOTIF_ID = 0;
	private static final int NOTIF_COLOR = 0xFF33B5E5;
	private static final int NOTIF_MS_LIGHT_ON = 250;
	private static final int NOTIF_MS_LIGHT_OFF = 500;
	private static boolean running = false;
	private boolean registered;
	private boolean wantToDisconnect;
	private boolean showActivities;
	private Handler handler;
	private Timer timerRegTimeout;
	private Timer timerKeepAlive;
	private ClientWriter threadWriter;
	private UserInfo uInfo;
	private int userId = DEF_USER_ID;
	private ArrayList<ChatInfoContainer> chatInfos;
	private UserListAdapter userLstAdapter;
	static CopyOnWriteArrayList<Message> msgsToSend;
	private UserListActivity userListAct;
	private ChatActivity chatAct;
	private static Client instance;

	public Client() {
		super("Client");
	}

	private void ini(Intent workIntent) {
		running = true;
		registered = false;
		wantToDisconnect = false;
		showActivities = workIntent.getBooleanExtra(ARG_SHOW_ACT, DEF_SHOW_ACT);
		instance = this;
		handler = new Handler(Looper.getMainLooper());
		timerRegTimeout = new Timer();
		timerKeepAlive = new Timer();
		userId = workIntent.getIntExtra(ARG_USER_ID, DEF_USER_ID);
		getSharedPreferences(ARG_USER_ID, MODE_PRIVATE).edit().putInt(ARG_USER_ID, userId).commit();
		chatInfos = new ArrayList<ChatInfoContainer>();
		userLstAdapter = new UserListAdapter(this, R.layout.chat_list_item, chatInfos);
		msgsToSend = new CopyOnWriteArrayList<Message>();
	}

	@Override
	protected void onHandleIntent(Intent workIntent) {
		ini(workIntent);
		Message msg = null;
		ChatInfoContainer auxTab = new ChatInfoContainer(0);

		Log.i(TAG, "Intentando registrarse con el id " + userId + "...");
		try {
			uInfo = new UserInfo(userId, SocketChannel.open(new InetSocketAddress(InetAddress.getByName(IP), PORT)));
			uInfo.getSock().configureBlocking(false);
		} catch (Exception e) {
			Log.e(TAG, "Error! " + e.getMessage());
			errorInRegister("No hay acceso a Internet.");
			running = false;
			return;
		}

		Log.i(TAG, "Conectado al servidor (" + uInfo.getSock().socket() + ")");
		threadWriter = new ClientWriter(uInfo.getSock());
		threadWriter.start();
		msgsToSend.add(new Message(EnumCommand.CMD_REGISTER, Message.VERSION, Message.HEADER_LEN, userId, 0, 0, EnumPayload.PAYLOAD_EMPTY, null));
		timerRegTimeout.schedule(new RegisterTimeout(), REGISTER_TIMEOUT);
		Log.i(TAG, "Mensaje de registro enviado!");

		while (!wantToDisconnect) {
			try {
				if (uInfo.getSock().read(uInfo.getBuf()) < 0) {
					Log.w(TAG, "El servidor cerró la conexión");
					wantToDisconnect = true;
					break;
				}
				while ((msg = uInfo.retrieveMsg()) != null) {
					Log.d(TAG, "Mensaje recibido: " + msg);
					auxTab.setDestId(msg.getOrigId()); // Tab representing conversation with user who sent you the message

					switch (msg.getCmd()) {
					case CMD_SEND:
						Log.i(TAG, "Mensaje del " + msg.getOrigId() + " recibido!");
						runOnUiThread(new UIRunner(auxTab, msg, true));
						msgsToSend.add(new Message(EnumCommand.SUCC_ANSWER_TO_SEND, Message.VERSION, Message.HEADER_LEN, userId, msg.getOrigId(), msg
								.getMsgId(), EnumPayload.PAYLOAD_EMPTY, null));
						break;
					case SUCC_ANSWER_TO_SEND:
						Log.d(TAG, "Primer tick");
						runOnUiThread(new UIRunner(auxTab, msg, false));
						break;
					case ERR_ANSWER_TO_SEND:
						Log.w(TAG, "Error en el primer tick");
						break;
					case CMD_DELIVERED:
						Log.d(TAG, "Segundo tick");
						runOnUiThread(new UIRunner(auxTab, msg, false));
						msgsToSend.add(new Message(EnumCommand.SUCC_ANSWER_TO_DELIVERED, Message.VERSION, Message.HEADER_LEN, userId,
								msg.getOrigId(), msg.getMsgId(), EnumPayload.PAYLOAD_EMPTY, null));
						break;
					case SUCC_ANSWER_TO_REGISTER:
						Log.i(TAG, "Conectado correctamente!");
						registered = true;
						timerRegTimeout.cancel();
						timerRegTimeout.purge();
						timerKeepAlive.schedule(new KeepAliveSocket(), 0, INTERVAL_KEEP_ALIVE_SOCKET);
						if (showActivities) {
							if (MainActivity.getInstance() != null) {
								MainActivity.getInstance().finish();
							} else {
								Log.e(TAG, "MainActivity.getInstance() es null y showActivities=true!");
							}
							startActivity(new Intent(this, UserListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
						}
						break;
					case ERR_ANSWER_TO_REGISTER:
						Log.w(TAG, "Error en register");
						timerRegTimeout.cancel();
						timerRegTimeout.purge();
						errorInRegister("Ya hay alguien conectado con ese Id. Por favor, pruebe con otro Id.");
						break;
					case SUCC_ANSWER_TO_UNREGISTER:
						Log.i(TAG, "Desconectado correctamente");
						wantToDisconnect = true;
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

		timerRegTimeout.cancel();
		timerRegTimeout.purge();
		timerKeepAlive.cancel();
		timerKeepAlive.purge();
		threadWriter.exit();

		registered = false;
		running = false;
		Log.w(TAG, "Voy a salir del cliente...!");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.w(TAG, "Saliendo del servicio del cliente! (Running = " + running + ")");
	}

	private void runOnUiThread(Runnable runnable) {
		handler.post(runnable);
	}

	public static boolean isRunning() {
		return running;
	}

	public static boolean isRegistered() {
		return (instance == null) ? false : instance.registered;
	}

	public static UserListAdapter getUserLstAdapter() {
		return instance.userLstAdapter;
	}

	public static int getUserId() {
		return instance.userId;
	}

	public static ArrayList<ChatInfoContainer> getChatInfos() {
		return instance.chatInfos;
	}

	public static UserListActivity getUserListAct() {
		return instance.userListAct;
	}

	public static void setUserListAct(UserListActivity userListAct) {
		instance.userListAct = userListAct;
	}

	public static void setChatAct(ChatActivity chatAct) {
		instance.chatAct = chatAct;
	}

	public static void exit() {
		instance.getSharedPreferences(ARG_USER_ID, MODE_PRIVATE).edit().putInt(ARG_USER_ID, DEF_USER_ID).commit();
		msgsToSend.add(new Message(EnumCommand.CMD_UNREGISTER, Message.VERSION, Message.HEADER_LEN, instance.userId, 0, 1, EnumPayload.PAYLOAD_EMPTY,
				null));
		instance.wantToDisconnect = true;
	}

	private void errorInRegister(String cause) {
		wantToDisconnect = true;
		if (MainActivity.getInstance() != null && showActivities) {
			MainActivity.getInstance().enableBtnConnect();
			runOnUiThread(new AlertDialogDisplay(cause));
		}
	}

	public static String removeEndLines(String origTxt) {
		String txt = origTxt;
		int last;

		while (((last = txt.length() - 1) >= 0) && (txt.charAt(last) == '\n' || txt.charAt(last) == ' ')) {
			txt = txt.substring(0, last);
		}

		while (((last = txt.length()) > 0) && (txt.charAt(0) == '\n' || txt.charAt(0) == ' ')) {
			txt = txt.substring(1, last);
		}

		return txt;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static void updateNotification(Message msg) {
		NotificationManager notifManager = (NotificationManager) instance.getSystemService(NOTIFICATION_SERVICE);
		int numUnreadUsers = getNumUnreadUsers(), numUnreadMsgs = getNumUnreadMessages();
		if (numUnreadUsers == 0) {
			notifManager.cancel(NOTIF_ID);
			return;
		}
		Intent intent = new Intent(instance, NotificationReceiver.class);
		PendingIntent pIntent = PendingIntent.getActivity(instance, 0, intent, 0);
		Notification.Builder n = new Notification.Builder(instance).setContentIntent(pIntent).setAutoCancel(true)
				.setLights(NOTIF_COLOR, NOTIF_MS_LIGHT_ON, NOTIF_MS_LIGHT_OFF).setNumber(numUnreadMsgs).setOnlyAlertOnce(false)
				.setSmallIcon(R.drawable.ic_launcher);
		if (msg != null) {
			n = n.setTicker(msg.getOrigId() + ": " + new String(msg.getPayload())).setVibrate(VIB_PATTERN);
		}

		if (numUnreadUsers == 1) {
			if (msg == null) {
				msg = getFirstUnreadMessage();
			}
			n = n.setLargeIcon(BitmapFactory.decodeResource(instance.getResources(), android.R.drawable.stat_notify_chat)).setContentTitle(
					String.valueOf(msg.getOrigId()));
			if (numUnreadMsgs == 1) {
				n = n.setContentText(new String(msg.getPayload()));
			} else {
				n = n.setContentText(numUnreadMsgs + " mensajes nuevos del " + msg.getOrigId());
			}
		} else {
			n = n.setLargeIcon(BitmapFactory.decodeResource(instance.getResources(), android.R.drawable.stat_notify_more))
					.setContentTitle(numUnreadUsers + " conversaciones sin leer")
					.setContentText(numUnreadMsgs + " mensajes nuevos de " + numUnreadUsers + " conversaciones");
		}

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) { // API Level 16+
			notifManager.notify(NOTIF_ID, n.build());
		} else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) { // API Level 11+
			notifManager.notify(NOTIF_ID, n.getNotification());
		}
	}

	public static void vibrate() {
		vibrate(VIB_PATTERN);
	}

	public static void vibrateError() {
		vibrate(VIB_ERROR_PATTERN);
	}

	public static void vibrate(long[] pattern) {
		Vibrator v;
		if (instance == null) {
			v = (Vibrator) MainActivity.getInstance().getSystemService(VIBRATOR_SERVICE);
		} else {
			v = (Vibrator) instance.getSystemService(VIBRATOR_SERVICE);
		}
		v.vibrate(pattern, -1);
	}

	private void addChatMessage(ChatInfoContainer chatInfo, Message msg) {
		ChatListAdapter lstAdapter = chatInfo.getListAdapter();
		lstAdapter.add(new ChatListItem(false, msg.getMsgId(), new String(msg.getPayload())));
		lstAdapter.notifyDataSetChanged();
		refreshUserList();

		if (chatAct == null || !chatAct.getChatInfo().equals(chatInfo)) {
			sortUserList();
			chatInfo.incUnread();
			updateNotification(msg);
			// vibrate();
		} else {
			chatAct.scrollListToBottom();
		}
	}

	private void addChatTick(ChatInfoContainer chatInfo, int msgId) {
		int pos = chatInfo.getMsgs().indexOf(new ChatListItem(false, msgId, ""));

		if (pos >= 0) {
			if (chatInfo.getMsgs().get(pos).hasTick1()) {
				chatInfo.getMsgs().get(pos).setTick2();
			} else {
				chatInfo.getMsgs().get(pos).setTick1();
			}
		} else {
			Log.w(TAG, "Tick recibido, pero no existe ningún mensaje con mId " + msgId);
		}
		chatInfo.getListAdapter().notifyDataSetChanged();
		refreshUserList();
	}

	public static void addUserToUsrList(int destId, boolean openUser) {
		ChatInfoContainer cic = new ChatInfoContainer(destId);

		cic.setListAdapter(new ChatListAdapter(instance, R.layout.chat_list_item, cic.getMsgs()));
		getUserLstAdapter().insert(cic, 0);
		getUserLstAdapter().notifyDataSetChanged();
		if (openUser) {
			openUserToChatWith(0);
		}
	}

	public static void removeUserFromUsrList(ChatInfoContainer chatInfo) {
		getUserLstAdapter().remove(chatInfo);
		getUserLstAdapter().notifyDataSetChanged();
	}

	public static void refreshUserList() {
		getUserLstAdapter().notifyDataSetChanged();
	}

	public static void sortUserList() {
		getUserLstAdapter().sort(new Comparador());
	}

	public static Message getFirstUnreadMessage() {
		if (instance == null)
			return null;

		for (int i = 0; i < instance.chatInfos.size(); i++) {
			if (instance.chatInfos.get(i).getUnread() > 0) {
				return instance.chatInfos.get(i).getLastMsg();
			}
		}

		return null;
	}

	public static int getNumUnreadMessages() {
		if (instance == null)
			return 0;
		int total = 0;

		for (int i = 0; i < instance.chatInfos.size(); i++) {
			total += instance.chatInfos.get(i).getUnread();
		}

		return total;
	}

	public static int getNumUnreadUsers() {
		if (instance == null)
			return 0;
		int total = 0;

		for (int i = 0; i < instance.chatInfos.size(); i++) {
			if (instance.chatInfos.get(i).getUnread() > 0) {
				total++;
			}
		}

		return total;
	}

	public static void openUnreadUserToChatWith() {
		for (int i = 0; i < instance.chatInfos.size(); i++) {
			if (instance.chatInfos.get(i).getUnread() > 0) {
				openUserToChatWith(i);
			}
		}
	}

	public static void openUserToChatWith(int position) {
		instance.startActivity(new Intent(instance, ChatActivity.class).putExtra(ChatActivity.ARG_CHAT_INFO, position).setFlags(
				Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}

	private class UIRunner implements Runnable {
		ChatInfoContainer auxTab;
		Message msg;
		boolean addMessage;

		public UIRunner(ChatInfoContainer auxTab, Message msg, boolean addMessage) {
			this.auxTab = auxTab;
			this.msg = msg;
			this.addMessage = addMessage;
		}

		@Override
		public void run() {
			int indTab = chatInfos.indexOf(auxTab);

			if (indTab < 0) {
				String txt = removeEndLines(new String(msg.getPayload()));
				if (txt.length() == 0) {
					return;
				} else {
					msg.setPayload(txt.getBytes());
				}
				addUserToUsrList(msg.getOrigId(), false);
				indTab = chatInfos.indexOf(auxTab);
			}

			if (addMessage) {
				addChatMessage(chatInfos.get(indTab), msg);
			} else {
				addChatTick(chatInfos.get(indTab), msg.getMsgId());
			}
		}
	}

	private class AlertDialogDisplay implements Runnable {
		String cause;

		public AlertDialogDisplay(String cause) {
			this.cause = cause;
		}

		@Override
		public void run() {
			MainActivity.ShowAlertDialog(userId, cause);
		}
	}

	private class RegisterTimeout extends TimerTask {
		@Override
		public void run() {
			Log.w(TAG, "Timeout en register");
			errorInRegister("Se excedió el tiempo de espera para el registro.");
		}
	}

	private class KeepAliveSocket extends TimerTask {
		@Override
		public void run() {
			if (isRegistered()) {
				Log.i(TAG, "Mandando mensaje de keep alive");
				msgsToSend.add(new Message(EnumCommand.SUCC_ANSWER_TO_REGISTER, Message.VERSION, Message.HEADER_LEN, userId, 0, 1,
						EnumPayload.PAYLOAD_EMPTY, null));
			} else {
				Log.e(TAG, "No se pudo mandar el mensaje de keep alive porque no estás registrado!");
			}
		}
	}

	public class ClientWriter extends Thread {
		private SocketChannel sock = null;
		private boolean wantToExit = false;

		public ClientWriter(SocketChannel sock) {
			this.sock = sock;
		}

		private void exit() {
			wantToExit = true;
		}

		private boolean sendMsg(Message msg) {
			try {
				ByteBuffer buf = ByteBuffer.wrap(msg.getBytes());
				while (buf.hasRemaining()) {
					sock.write(buf);
				}
				Log.d(TAG, "Mensaje enviado: " + msg);
			} catch (Exception e) {
				Log.e(TAG, "No ha sido posible mandar el siguiente mensaje. " + e.getMessage());
				Log.w(TAG, "Se intentaba enviar este mensaje: " + msg);
				msgsToSend.add(msg);
				return false;
			}

			return true;
		}

		private void iterate() {
			Iterator<Message> it = msgsToSend.iterator();
			int pos;

			while (it.hasNext()) {
				if ((pos = msgsToSend.indexOf(it.next())) >= 0) {
					sendMsg(msgsToSend.remove(pos));
				}
			}
		}

		@Override
		public void run() {
			while (!wantToExit) {
				iterate();
				synchronized (this) {
					try {
						sleep(100);
					} catch (Exception e) {
					}
				}
			}
			iterate();

			try {
				sock.close();
			} catch (IOException e) {
				Log.e(TAG, "No se ha podido el cerrar el socket antes de salir! " + e.getMessage());
			}
		}
	}

}
