package uc3m.apptel;

import uc3m.apptel.utils.ChatInfoContainer;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.michaelnovakjr.numberpicker.NumberPicker;
import com.michaelnovakjr.numberpicker.NumberPickerDialog;
import com.michaelnovakjr.numberpicker.NumberPickerDialog.OnNumberSetListener;

public class UserListActivity extends Activity {
	private ListView lst;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_list);

		lst = (ListView) findViewById(R.id.lstUsers);
		lst.setAdapter(Client.getUserLstAdapter());
		lst.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Client.openUserToChatWith(position);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Client.setUserListAct(this);
		Client.sortUserList();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Client.setUserListAct(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.user_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_unregister:
			Client.exit();
			finish();
			return true;
		case R.id.action_new_user:
			chatWithNewUser();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void chatWithNewUser() {
		NumberPickerDialog dialog;
		NumberPicker np;

		dialog = new NumberPickerDialog(this, 0, 0, "¿Con quién quieres hablar?", getText(android.R.string.ok), getText(android.R.string.cancel));
		np = dialog.getNumberPicker();
		np.setRange(Client.MIN_USER_ID, Client.MAX_USER_ID);
		np.setWrap(true);
		dialog.setOnNumberSetListener(new OnNumberSetListener() {
			@Override
			public void onNumberSet(int selectedNumber) {
				int pos;

				if (selectedNumber == Client.getUserId()) {
					Toast.makeText(Client.getUserListAct(), "¡No puedes hablar contigo mismo!", Toast.LENGTH_SHORT).show();
				} else if (selectedNumber < Client.MIN_USER_ID || selectedNumber > Client.MAX_USER_ID) {
					Toast.makeText(Client.getUserListAct(), "¡Introduce un id válido!", Toast.LENGTH_SHORT).show();
				} else if ((pos = Client.getChatInfos().indexOf(new ChatInfoContainer(selectedNumber))) >= 0) {
					Client.openUserToChatWith(pos);
				} else {
					Client.addUserToUsrList(selectedNumber, true);
				}
			}
		});
		dialog.show();
	}

}
