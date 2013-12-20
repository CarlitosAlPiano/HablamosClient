package uc3m.apptel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void btnConnect_onClick(View view) {
		Log.w(this.getString(R.string.app_name), "Boton Connect pulsado");
		(new Client(this, Integer.parseInt(((EditText) findViewById(R.id.txtUsuario)).getText().toString()))).start();
		view.setEnabled(false);
		((Button) view).setText("Conectando...");
	}
}
