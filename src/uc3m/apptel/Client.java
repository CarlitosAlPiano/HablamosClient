package uc3m.apptel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import uc3m.apptel.utils.EnumCommand;
import uc3m.apptel.utils.EnumPayload;
import uc3m.apptel.utils.Message;
import uc3m.apptel.utils.UserInfo;
import android.util.Log;

public class Client extends Thread {

	private static final String TAG = "HablamosClient";
	private static final int PORT = 8888;
	private static final String IP = "192.168.0.10";
	private UserInfo uInfo = null;
	private MainActivity mainAct = null;
	private int userNum = 0;
	
	public Client(MainActivity mainAct) {
		this.mainAct = mainAct;
	}

	public void setUserNum(int userNum) {
		this.userNum = userNum;
	}

	public boolean writeMsg(Message msg) {
		try {
			ByteBuffer buf = ByteBuffer.wrap(msg.getBytes());
			while(buf.hasRemaining()){
				uInfo.getSock().write(buf);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}

		return true;
	}
	
	@Override
	public void run() {
		Message msg = null;

		Log.i(TAG, "Conectando con el servidor...");
		try {
			uInfo = new UserInfo(userNum, SocketChannel.open(new InetSocketAddress(InetAddress.getByName(IP), PORT)));
			uInfo.getSock().configureBlocking(false);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return;
		}
		
		Log.i(TAG, "Conectado! " + uInfo.getSock().socket());
		writeMsg(new Message(EnumCommand.CMD_REGISTER, Message.VERSION, Message.HEADER_LEN, userNum, 0, 1, EnumPayload.PAYLOAD_EMPTY, null));
		Log.i(TAG, "Mensaje de registro enviado!");
		
		while(true) {
			try {
				uInfo.getSock().read(uInfo.getBuf());
				while ((msg = uInfo.retrieveMsg()) != null) {
					System.out.println("Message received: " + msg);
					switch (msg.getCmd()) {
					case CMD_SEND:
						Log.i(TAG, "Mensaje recibido!! :)");
						// Mostrar en la pantalla
						// Contestar: SUCC_ANS_TO_SEND
						break;
					case SUCC_ANSWER_TO_SEND:
						Log.i(TAG,"Primer tick");
						break;
					case ERR_ANSWER_TO_SEND:
						Log.i(TAG,"Error en el primer tick. Retrying");
						// Retry ()
						break;
					case CMD_DELIVERED:
						Log.i(TAG,"Segundo tick");
						break;
					case SUCC_ANSWER_TO_REGISTER:
						Log.i(TAG,"Conectado correctamente");
						mainAct.showConnectedView(userNum);
						break;
					case ERR_ANSWER_TO_REGISTER:
						Log.w(TAG,"Error en register");
						break;
					case SUCC_ANSWER_TO_UNREGISTER:
						Log.i(TAG,"Desconectado correctamente");
						break;
					case ERR_ANSWER_TO_UNREGISTER:
						// Posible reintento hasta recibir SUCC_...
						break;
					case CMD_REGISTER:
					case CMD_UNREGISTER:
					case SUCC_ANSWER_TO_DELIVERED:
					case ERR_ANSWER_TO_DELIVERED:
					default:	// Server is not supposed to receive those
						break;
					}
				}
			} catch (Exception e) {
				Log.e(TAG,e.getMessage());
			}
		}
	}

}
