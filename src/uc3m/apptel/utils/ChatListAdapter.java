package uc3m.apptel.utils;

import java.util.List;

import uc3m.apptel.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class ChatListAdapter extends ArrayAdapter<ChatListItem> {
	private List<ChatListItem> items;

	public ChatListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public ChatListAdapter(Context context, int resource,
			List<ChatListItem> items) {
		super(context, resource, items);

		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ChatListItem item = items.get(position);

		if (v == null) {
			LayoutInflater vi;
			vi = LayoutInflater.from(getContext());
			v = vi.inflate(R.layout.chat_list_item, null);
		}

		if (item != null) {
			TextView txt = (TextView) v.findViewById(R.id.chatItemTxtMessage);
			ImageView img = (ImageView) v.findViewById(R.id.chatItemImgMessage);

			txt.setText(item.getMsg());
			if (item.wasSentByUser()) {
				if(item.hasTick2()) {
					img.setImageResource(android.R.drawable.ic_media_ff);
				} else if(item.hasTick1()) {
					img.setImageResource(android.R.drawable.ic_media_play);
				} else {
					img.setImageResource(android.R.drawable.ic_media_pause);
				}
			} else {
				LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, img.getLayoutParams().height);
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
				img.setLayoutParams(params);
				img.setImageResource(android.R.drawable.ic_media_rew);
				
				params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
				params.addRule(RelativeLayout.LEFT_OF, R.id.chatItemImgMessage);
				txt.setLayoutParams(params);
			}
		}

		return v;
	}
}
