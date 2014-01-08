package uc3m.apptel;

import java.util.ArrayList;

import uc3m.apptel.utils.ChatInfoContainer;
import uc3m.apptel.utils.ChatListAdapter;
import uc3m.apptel.utils.UserListAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class UserListActivity extends Activity {
	public static final String ARG_USER_ID = "user_id";
	public static final int DEF_USER_ID = 0;
	private static int userId;
	private ListView lst;
	private static UserListAdapter lstAdapter = null;
	private static ArrayList<ChatInfoContainer> chatInfos = new ArrayList<ChatInfoContainer>();
	private static UserListActivity instance;

	public UserListActivity() {
		instance = this;
	}

	public static UserListActivity getInstance() {
		return instance;
	}

	public static int getUserId() {
		return userId;
	}

	public static ArrayList<ChatInfoContainer> getChatInfos() {
		return chatInfos;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_list);

		userId = getIntent().getIntExtra(ARG_USER_ID, DEF_USER_ID);
		getSharedPreferences(ARG_USER_ID, MODE_PRIVATE).edit().putInt(ARG_USER_ID, userId).commit();
		lst = (ListView) findViewById(R.id.lstUsers);
		lstAdapter = new UserListAdapter(this, R.layout.chat_list_item, chatInfos);
		lst.setAdapter(lstAdapter);
		lst.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				openUser(position);
			}
		});
	}

	public static void addUser(int destId, boolean openUser) {
		ChatInfoContainer cic = new ChatInfoContainer(destId);

		cic.setListAdapter(new ChatListAdapter(getInstance(), R.layout.chat_list_item, cic.getMsgs()));
		lstAdapter.insert(cic, 0);
		lstAdapter.notifyDataSetChanged();
		if (openUser)
			openUser(0);
	}

	public static void removeUser(ChatInfoContainer chatInfo) {
		lstAdapter.remove(chatInfo);
		lstAdapter.notifyDataSetChanged();
	}

	public static void openUser(int position) {
		Intent intent = new Intent(getInstance(), ChatActivity.class);
		intent.putExtra(ChatActivity.ARG_CHAT_INFO, position);
		getInstance().startActivity(intent);
	}

	public static void updateList() {
		lstAdapter.notifyDataSetChanged();
	}

	public static void sortList() {
		updateList();
	}

	public void btnChatWithNewUser_onClick(View view) {
		int uId = 1001;

		while (chatInfos.contains(new ChatInfoContainer(uId)) || uId == userId) {
			uId++;
		}
		addUser(uId, true);
	}

}
