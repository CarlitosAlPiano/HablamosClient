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

	public ChatListAdapter(Context context, int resource, List<ChatListItem> items) {
		super(context, resource, items);
		this.items = items;
	}

	private String limitMsgText(String origTxt) {
		String txt = origTxt;
		boolean isPartialText = false;
		int pos, txtLimit = 30;

		if ((pos = txt.indexOf('\n')) >= 0) {
			txt = txt.substring(0, pos);
			isPartialText = true;
		}
		if (txt.length() > txtLimit) {
			pos = txt.lastIndexOf(' ', txtLimit);
			if (pos < 0) {
				txt = txt.substring(0, txtLimit);
			} else {
				txt = txt.substring(0, pos);
			}
			isPartialText = true;
		}
		if (isPartialText) {
			txt += "...";
		}

		return txt;
	}

	public void inflate(int position, TextView txt, ImageView img, boolean changeLayout) {
		if (position < 0) {
			txt.setText("Escríbele algo a tu amigo.");
			img.setImageResource(android.R.drawable.ic_menu_edit);
			return;
		}
		ChatListItem item = items.get(position);
		LayoutParams paramsImg = new LayoutParams(LayoutParams.WRAP_CONTENT, img.getLayoutParams().height);
		LayoutParams paramsTxt = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		if (item != null) {
			txt.setText(changeLayout ? item.getMsg() : limitMsgText(item.getMsg()));
			if (item.wasSentByUser()) {
				if (item.hasTick2()) {
					img.setImageResource(android.R.drawable.ic_media_ff);
				} else if (item.hasTick1()) {
					img.setImageResource(android.R.drawable.ic_media_play);
				} else {
					img.setImageResource(android.R.drawable.ic_media_pause);
				}
			} else {
				img.setImageResource(android.R.drawable.ic_media_rew);
			}

			if (changeLayout) {
				paramsImg.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
				paramsImg.addRule(item.wasSentByUser() ? RelativeLayout.ALIGN_PARENT_LEFT : RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
				img.setLayoutParams(paramsImg);
				paramsTxt.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
				paramsTxt.addRule(item.wasSentByUser() ? RelativeLayout.RIGHT_OF : RelativeLayout.LEFT_OF, R.id.chatItemImgMessage);
				txt.setLayoutParams(paramsTxt);
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.chat_list_item, null);
		}
		inflate(position, (TextView) v.findViewById(R.id.chatItemTxtMessage), (ImageView) v.findViewById(R.id.chatItemImgMessage), true);

		return v;
	}
}
