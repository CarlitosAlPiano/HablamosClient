package uc3m.apptel;

import uc3m.apptel.utils.ChatInfoContainer;
import uc3m.apptel.utils.ChatListItem;
import uc3m.apptel.utils.EnumCommand;
import uc3m.apptel.utils.EnumPayload;
import uc3m.apptel.utils.Message;
import uc3m.apptel.utils.MyListView;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class ChatActivity extends Activity {
	public static final String ARG_CHAT_INFO = "chat_info";
	public static final int DEF_CHAT_INFO = 0;
	private ChatInfoContainer chatInfo;
	private MyListView list;
	private ImageButton btnSend;
	private EditText txtMsg;
	private static ChatActivity instance;

	public static ChatActivity getInstance() {
		return instance;
	}

	public static void scrollDownList() {
		if (getInstance() == null)
			return;
		getInstance().scrollList();
	}

	public void scrollList() {
		list.setSelection(list.getCount() - 1);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		instance = this;

		list = (MyListView) findViewById(R.id.lstConversacion);
		btnSend = (ImageButton) findViewById(R.id.btnSendMessage);
		txtMsg = (EditText) findViewById(R.id.txtNewMessage);
		chatInfo = UserListActivity.getChatInfos().get(getIntent().getIntExtra(ARG_CHAT_INFO, DEF_CHAT_INFO));
		list.setAdapter(chatInfo.getListAdapter());
		btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = txtMsg.getText().toString();
				Client.msgsToSend.add(new Message(EnumCommand.CMD_SEND, Message.VERSION, Message.HEADER_LEN + text.length(), UserListActivity
						.getUserId(), chatInfo.getDestId(), chatInfo.getMsgId(), EnumPayload.PAYLOAD_TEXT, text.getBytes()));
				chatInfo.getListAdapter().add(new ChatListItem(true, chatInfo.getMsgId(), text));
				chatInfo.getListAdapter().notifyDataSetChanged();
				chatInfo.incMsgId();
				txtMsg.setText("");
				scrollList();
			}
		});
		scrollList();
	}

	@Override
	public void onBackPressed() {
		if (chatInfo.getMsgs().size() <= 0) {
			UserListActivity.removeUser(chatInfo);
		}
		UserListActivity.sortList();
		finish();
	}
}
