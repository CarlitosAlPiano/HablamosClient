package uc3m.apptel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class NotificationReceiver extends Activity {
	private class OpenNotif extends Thread {
		private Context ctx;

		public OpenNotif(Context ctx) {
			this.ctx = ctx;
		}

		@Override
		public void run() {
			synchronized (this) {
				try {
					int i = 0;
					while (!Client.isRunning()) {
						wait(500);
						i++;
						if (i >= 10)
							throw new Exception();
					}
				} catch (Exception e) {
					Log.e(Client.TAG, "Se ha pulsado una notificación pero el cliente no puede registrarse...");
					return;
				}
			}

			startActivity(new Intent(ctx, UserListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			if (Client.getNumUnreadUsers() == 1) {
				Client.openUnreadUserToChatWith();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(Client.TAG, "El usuario ha pulsado en la notificacion!");
		if (KeepAliveClientService.reopenClient(this, false)) { // Me aseguro de que el cliente esta corriendo y conectado
			new OpenNotif(this).start();
		}
		finish();
	}
}
