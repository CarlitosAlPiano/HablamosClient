package uc3m.apptel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class KeepAliveClientService extends BroadcastReceiver {
	public KeepAliveClientService() {
	}

	public static void reopenClient(Context context, boolean isBroadcast) {
		String msg = (isBroadcast) ? "Broadcast recibido" : "Notificación recibida";

		if (MainActivity.getInstance() == null && !Client.isRunning()) {
			int uId = context.getSharedPreferences(Client.ARG_USER_ID, Context.MODE_PRIVATE).getInt(Client.ARG_USER_ID, Client.DEF_USER_ID);
			Log.i(Client.TAG, msg + "! Leído el id " + uId);
			if (uId != Client.DEF_USER_ID) {
				Intent service = new Intent(context, Client.class).putExtra(Client.ARG_USER_ID, uId).putExtra(Client.ARG_SHOW_ACT, false);
				context.startService(service);
			}
		} else {
			Log.i(Client.TAG, msg + "! No haré nada porque el cliente ya está corriendo...");
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		reopenClient(context, true);
		while (!Client.isRunning()) {
			try {
				wait(100);
			} catch (InterruptedException e) {
				Log.e(Client.TAG, "Error esperando a que arranque el cliente");
			}
		}
		Client.updateNotification(null);
	}

}
