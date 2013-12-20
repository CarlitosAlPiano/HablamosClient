package uc3m.apptel;

import java.util.ArrayList;

import uc3m.apptel.utils.ChatInfoContainer;
import uc3m.apptel.utils.ChatListAdapter;
import uc3m.apptel.utils.ChatListItem;
import uc3m.apptel.utils.EnumCommand;
import uc3m.apptel.utils.EnumPayload;
import uc3m.apptel.utils.Message;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class ChatActivity extends FragmentActivity {
	public static final String ARG_USER_ID = "user_id";
	static ArrayList<ChatInfoContainer> chatInfos = new ArrayList<ChatInfoContainer>();
	static SectionsPagerAdapter tabsAdapter;
	private static int userId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		userId = getIntent().getIntExtra(ChatActivity.ARG_USER_ID, 1001);

		// Create the adapter that will return a fragment for each 'tab'
		tabsAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		// Set up the ViewPager with the sections adapter.
		((ViewPager) findViewById(R.id.pager)).setAdapter(tabsAdapter);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);

			for (int i = 1001; i < 1005; i++) {
				chatInfos.add(new ChatInfoContainer(i));
			}
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			Fragment fragment = new ChatFragment();
			Bundle args = new Bundle();
			args.putInt(ChatFragment.ARG_CHAT_INFO, position);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return chatInfos.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "Hablar con el " + chatInfos.get(position).getDestId();
		}
	}

	public static class ChatFragment extends Fragment {
		public static final String ARG_CHAT_INFO = "chat_info";
		private ChatInfoContainer chatInfo;
		private ListView list;
		private ImageButton btnSend;
		private EditText txtMsg;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
			list = (ListView) rootView.findViewById(R.id.lstConversacion);
			btnSend = (ImageButton) rootView.findViewById(R.id.btnSendMessage);
			txtMsg = (EditText) rootView.findViewById(R.id.txtNewMessage);
			chatInfo = chatInfos.get(getArguments().getInt(ChatFragment.ARG_CHAT_INFO));
			chatInfo.setListAdapter(new ChatListAdapter(this.getActivity(), R.layout.chat_list_item, chatInfo.getMsgs()));
			list.setAdapter(chatInfo.getListAdapter());
			btnSend.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					String text = txtMsg.getText().toString();
					Client.msgsToSend.add(new Message(EnumCommand.CMD_SEND, Message.VERSION, Message.HEADER_LEN+text.length(), userId, chatInfo.getDestId(), chatInfo.getMsgId(), EnumPayload.PAYLOAD_TEXT, text.getBytes()));
					chatInfo.getListAdapter().add(new ChatListItem(true, chatInfo.getMsgId(), text));
					chatInfo.getListAdapter().notifyDataSetChanged();
					chatInfo.incMsgId();
					txtMsg.setText("");
				}
			});

			return rootView;
		}
	}

}
