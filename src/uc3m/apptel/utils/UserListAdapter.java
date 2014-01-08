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
			TextView txt = (TextView) v.findViewById(R.id.userName);
			View lastMsg = v.findViewById(R.id.userLastMessage);
			if(item.getListAdapter() != null) {
				item.getListAdapter().inflate(item.getMsgs().size() - 1, (TextView) lastMsg.findViewById(R.id.chatItemTxtMessage),
					(ImageView) lastMsg.findViewById(R.id.chatItemImgMessage), false);
			}
			txt.setText(String.valueOf(item.getDestId()));
		}

		return v;
	}
}
