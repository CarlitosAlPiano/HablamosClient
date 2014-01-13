package uc3m.apptel;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
	private Button btnConnect;
	private static MainActivity instance;

	public static MainActivity getInstance() {
		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (Client.isRegistered()) {
			Log.i(Client.TAG, "Se ha detectado que el servicio del cliente ya esta corriendo, abro la lista de usuarios!");
			startActivity(new Intent(this, UserListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			finish();
			return;
		}

		avoidGettingServiceKilled();
		btnConnect = (Button) findViewById(R.id.btnConnect);
		btnConnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					connect(Integer.parseInt(((EditText) findViewById(R.id.txtUsuario)).getText().toString()));
				} catch (NumberFormatException e) {
					ShowAlertDialog(0, "El Id tiene que estar entre " + Client.MIN_USER_ID + " y " + Client.MAX_USER_ID
							+ ".\nPor favor, introduzca un Id válido.");
				}
			}
		});
		enableBtnConnect();
		int uId = getSharedPreferences(Client.ARG_USER_ID, MODE_PRIVATE).getInt(Client.ARG_USER_ID, Client.DEF_USER_ID);
		if (uId != Client.DEF_USER_ID) {
			((EditText) findViewById(R.id.txtUsuario)).setText(String.valueOf(uId));
			connect(uId);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		instance = this;
	}

	@Override
	protected void onStop() {
		super.onStop();
		instance = null;
	}

	public void enableBtnConnect() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				btnConnect.setText(R.string.btn_connect);
				btnConnect.setEnabled(true);
				btnConnect.setTextColor(getResources().getColor(R.color.white));
			}
		});
	}

	private void connect(int userId) {
		if (userId < Client.MIN_USER_ID || userId > Client.MAX_USER_ID) {
			ShowAlertDialog(userId, "El Id tiene que estar entre " + Client.MIN_USER_ID + " y " + Client.MAX_USER_ID + ".");
		} else {
			btnConnect.setEnabled(false);
			btnConnect.setTextColor(getResources().getColor(R.color.black_overlay));
			btnConnect.setText(R.string.btn_connecting);
			startService(new Intent(this, Client.class).putExtra(Client.ARG_USER_ID, userId).putExtra(Client.ARG_SHOW_ACT, true));
		}
	}

	public static void ShowAlertDialog(int userId, String msg) {
		String err = "No ha sido posible iniciar sesión" + ((userId == 0) ? "" : " con el id " + userId);
		new AlertDialog.Builder(instance).setTitle("Error!").setMessage(err + ".\n" + msg).setPositiveButton(android.R.string.ok, null)
				.setIcon(android.R.drawable.ic_dialog_alert).show();
		Client.vibrateError();
	}

	private void avoidGettingServiceKilled() {
		Intent intent = new Intent(this, KeepAliveClientService.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Client.INTERVAL_KEEP_ALIVE_SERVICE,
				Client.INTERVAL_KEEP_ALIVE_SERVICE, pendingIntent);
	}
}
