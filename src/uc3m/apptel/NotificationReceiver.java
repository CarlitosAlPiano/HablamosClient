package uc3m.apptel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class NotificationReceiver extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(Client.TAG, "El usuario ha pulsado en la notificacion!");
		KeepAliveClientService.reopenClient(this, false); // Me aseguro de que el cliente esta corriendo y conectado

		if (!Client.isRegistered()) {
			try {
				wait(500);
				if (!Client.isRegistered()) {
					wait(4000);
					if (!Client.isRegistered()) {
						throw new Exception();
					}
				}
			} catch (Exception e) {
				Log.e(Client.TAG, "Se ha pulsado una notificación pero el cliente no puede registrarse...");
				// Client.updateNotification(null);
				finish();
				return;
			}
		}

		startActivity(new Intent(this, UserListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		if (Client.getNumUnreadUsers() == 1) {
			Client.openUnreadUserToChatWith();
		}
		finish();
	}

}
