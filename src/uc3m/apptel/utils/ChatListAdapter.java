package uc3m.apptel.utils;

import java.util.List;
import uc3m.apptel.R;
import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.text.format.Time;
import android.view.Gravity;
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

	public void inflate(int position, ImageView img, TextView txt, TextView date, boolean dateAlwaysAsTime, boolean changeLayout) {
		if (position < 0) {
			img.setImageResource(android.R.drawable.ic_menu_edit);
			txt.setText("Escríbele algo a tu amigo.");
			date.setText("");
			return;
		}
		ChatListItem item = items.get(position);
		LayoutParams paramsImg = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); // img.getLayoutParams().height);
		LayoutParams paramsTxt = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		LayoutParams paramsDate = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		if (item != null) {
			txt.setText(item.getMsg());
			if (dateAlwaysAsTime) {
				date.setText(item.getTime().format("%k:%M")); // Ej: 0:43
			} else {
				Time today = new Time(), yesterday = new Time(), year = new Time();
				today.setJulianDay(Time.getJulianDay(today.toMillis(true), today.gmtoff));
				yesterday.setJulianDay(Time.getJulianDay(today.toMillis(true), today.gmtoff) - 1);
				year.setJulianDay(Time.getJulianDay(today.toMillis(true), today.gmtoff) - today.yearDay);

				if (Time.compare(item.getTime(), today) >= 0) {
					date.setText(item.getTime().format("%k:%M")); // Ej: 0:43
				} else if (Time.compare(item.getTime(), yesterday) >= 0) {
					date.setText("Ayer");
				} else if (Time.compare(item.getTime(), year) >= 0) {
					date.setText(item.getTime().format("%e %b")); // Ej: 5 Feb
				} else {
					date.setText(item.getTime().format("%e %b '%g")); // Ej: 5 Feb '12
				}
			}
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
				if (item.wasSentByUser()) {
					paramsImg.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
					paramsTxt.addRule(RelativeLayout.RIGHT_OF, R.id.chatItemImgMessage);
					paramsTxt.addRule(RelativeLayout.LEFT_OF, R.id.chatItemDateMessage);
					txt.setGravity(Gravity.LEFT);
					paramsDate.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
				} else {
					paramsImg.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
					paramsTxt.addRule(RelativeLayout.LEFT_OF, R.id.chatItemImgMessage);
					paramsTxt.addRule(RelativeLayout.RIGHT_OF, R.id.chatItemDateMessage);
					txt.setGravity(Gravity.RIGHT);
					paramsDate.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
				}
				paramsImg.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.chatItemDateMessage);
				img.setLayoutParams(paramsImg);
				txt.setLayoutParams(paramsTxt);
				date.setLayoutParams(paramsDate);
			} else {
				txt.setMaxLines(1);
				txt.setEllipsize(TruncateAt.END);
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.chat_list_item, null);
		}
		inflate(position, (ImageView) v.findViewById(R.id.chatItemImgMessage), (TextView) v.findViewById(R.id.chatItemTxtMessage),
				(TextView) v.findViewById(R.id.chatItemDateMessage), true, true);
		v.setBackgroundColor(v.getResources().getColor(R.color.azul_debil));

		return v;
	}
}
