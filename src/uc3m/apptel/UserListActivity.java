package uc3m.apptel;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class UserListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_list);

		String myId = getIntent().getStringExtra("MyID");
		TextView t = (TextView)findViewById(R.id.txtMyID);
		t.setText(myId);
	}
	
	public void fila_onClick(View view) {
		Log.i("HablamosClient", "Fila pulsada");
		switch(view.getId()) {
		case R.id.fila1001:
			Log.i("HablamosClient", "1001");
			break;
		case R.id.fila1002:
			Log.i("HablamosClient", "1002");
			break;
		default:
			Log.i("HablamosClient", "1003");
			break;
		}
	}

}
