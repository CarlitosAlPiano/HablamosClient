package uc3m.apptel;

import uc3m.apptel.utils.ChatInfoContainer;
import uc3m.apptel.utils.ChatListItem;
import uc3m.apptel.utils.EnumCommand;
import uc3m.apptel.utils.EnumPayload;
import uc3m.apptel.utils.Message;
import uc3m.apptel.utils.MyListView;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		list = (MyListView) findViewById(R.id.lstConversacion);
		btnSend = (ImageButton) findViewById(R.id.btnSendMessage);
		txtMsg = (EditText) findViewById(R.id.txtNewMessage);
		chatInfo = Client.getChatInfos().get(getIntent().getIntExtra(ARG_CHAT_INFO, DEF_CHAT_INFO));
		setTitle(String.valueOf(chatInfo.getDestId()));
		list.setAdapter(chatInfo.getListAdapter());
		btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = Client.removeEndLines(txtMsg.getText().toString());
				txtMsg.setText("");
				if (text.length() == 0) {
					return;
				}
				Client.msgsToSend.add(new Message(EnumCommand.CMD_SEND, Message.VERSION, Message.HEADER_LEN + text.length(), Client.getUserId(),
						chatInfo.getDestId(), chatInfo.getMsgId(), EnumPayload.PAYLOAD_TEXT, text.getBytes()));
				chatInfo.getListAdapter().add(new ChatListItem(true, chatInfo.getMsgId(), text));
				chatInfo.getListAdapter().notifyDataSetChanged();
				chatInfo.incMsgId();
				scrollListToBottom();
			}
		});
		scrollListToBottom();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Client.setChatAct(this);
		chatInfo.rstUnread();
		Client.updateNotification(null);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Client.setChatAct(null);
		if (chatInfo.getMsgs().size() <= 0) {
			Client.removeUserFromUsrList(chatInfo);
		}
		Client.sortUserList();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	public ChatInfoContainer getChatInfo() {
		return chatInfo;
	}

	public void scrollListToBottom() {
		list.setSelection(list.getCount() - 1);
	}
}
