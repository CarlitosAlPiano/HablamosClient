package uc3m.apptel.utils;

import java.util.List;

import uc3m.apptel.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserListAdapter extends ArrayAdapter<ChatInfoContainer> {
	private List<ChatInfoContainer> items;

	public UserListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public UserListAdapter(Context context, int resource, List<ChatInfoContainer> items) {
		super(context, resource, items);
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ChatInfoContainer item = items.get(position);

		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.user_list_item, null);
		}

		if (item != null) {
			TextView txtDest = (TextView) v.findViewById(R.id.userName);
			TextView txtUnread = (TextView) v.findViewById(R.id.userNumUnreadMessages);
			View lastMsg = v.findViewById(R.id.userLastMessage);
			if (item.getListAdapter() != null) {
				item.getListAdapter().inflate(item.getMsgs().size() - 1, (ImageView) lastMsg.findViewById(R.id.chatItemImgMessage),
						(TextView) lastMsg.findViewById(R.id.chatItemTxtMessage), (TextView) v.findViewById(R.id.userDateLastMessage), false, false);
			}
			txtDest.setText(String.valueOf(item.getDestId()));
			if (item.getUnread() > 0) {
				txtUnread.setBackgroundResource(R.drawable.num_unread_messages);
				txtUnread.setText(String.valueOf(item.getUnread()));
				v.setBackgroundColor(v.getResources().getColor(R.color.azul_fuerte));
				lastMsg.setBackgroundColor(v.getResources().getColor(R.color.azul_fuerte));
			} else {
				txtUnread.setBackgroundResource(0);
				txtUnread.setText("");
				v.setBackgroundColor(v.getResources().getColor(R.color.azul_debil));
				lastMsg.setBackgroundColor(v.getResources().getColor(R.color.azul_debil));
			}
		}

		return v;
	}
}
