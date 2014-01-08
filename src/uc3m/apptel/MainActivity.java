package uc3m.apptel;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
	private Button btnConnect;

	private void connect(int userId) {
		btnConnect.setEnabled(false);
		btnConnect.setText("Conectando...");
		Log.i(this.getString(R.string.app_name), "Conectando con el id " + userId + "...");
		new Client(this, userId).start();
	}

	public void enableBtnConnect() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				btnConnect.setText(R.string.btn_connect);
				btnConnect.setEnabled(true);
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnConnect = (Button) findViewById(R.id.btnConnect);
		int uId = getSharedPreferences(UserListActivity.ARG_USER_ID, MODE_PRIVATE).getInt(UserListActivity.ARG_USER_ID, UserListActivity.DEF_USER_ID);
		if (uId != UserListActivity.DEF_USER_ID) {
			((EditText) findViewById(R.id.txtUsuario)).setText(String.valueOf(uId));
			connect(uId);
		}
	}

	public void btnConnect_onClick(View view) {
		connect(Integer.parseInt(((EditText) findViewById(R.id.txtUsuario)).getText().toString()));
	}
}
