package uc3m.apptel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class KeepAliveClientService extends BroadcastReceiver {
	private class UpdateNotif extends Thread {
		@Override
		public void run() {
			synchronized (this) {
				try {
					int i = 0;
					while (!Client.isRunning()) {
						sleep(500);
						i++;
						if (i >= 20)
							throw new Exception();
					}
					Client.updateNotification(null);
				} catch (Exception e) {
					Log.e(Client.TAG, "Error esperando a que arranque el cliente");
					return;
				}
			}
		}
	}

	public KeepAliveClientService() {
	}

	public static boolean reopenClient(Context context, boolean isBroadcast) {
		String msg = (isBroadcast) ? "Broadcast recibido" : "Notificación recibida";

		if (MainActivity.getInstance() == null && !Client.isRunning()) {
			int uId = context.getSharedPreferences(Client.ARG_USER_ID, Context.MODE_PRIVATE).getInt(Client.ARG_USER_ID, Client.DEF_USER_ID);
			Log.i(Client.TAG, msg + "! Leído el id " + uId);
			if (uId == Client.DEF_USER_ID) {
				return false;
			} else {
				Intent service = new Intent(context, Client.class).putExtra(Client.ARG_USER_ID, uId).putExtra(Client.ARG_SHOW_ACT, false);
				context.startService(service);
			}
		} else {
			Log.i(Client.TAG, msg + "! No haré nada porque el cliente ya está corriendo...");
		}

		return true;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (reopenClient(context, true)) {
			new UpdateNotif().start();
		}
	}

}
