package com.ducnd.myadapter;

import java.util.ArrayList;

import com.ducnd.democall_message_sinch.R;
import com.ducnd.myitem.Item_ListUser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyAdaper_ListUser extends BaseAdapter {
	private ArrayList<Item_ListUser> arrListUser;
	private Context mContext;
	private LayoutInflater mInflater;

	public MyAdaper_ListUser(Context context,
			ArrayList<Item_ListUser> arrListUser) {
		this.mContext = context;
		this.arrListUser = arrListUser;
		this.mInflater = LayoutInflater.from(this.mContext);
	}

	@Override
	public int getCount() {
		return arrListUser.size();
	}

	@Override
	public Item_ListUser getItem(int position) {
		return arrListUser.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HolderView holder = null;
		if ( convertView == null ) {
			convertView = mInflater.inflate(R.layout.item_info_user, parent, false);
			holder = new HolderView();
			holder.textUsername = (TextView)convertView.findViewById(R.id.textUsername);
			holder.textDate = (TextView)convertView.findViewById(R.id.textDate);
			holder.icon = (ImageView)convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
		}
		else {
			holder = (HolderView)convertView.getTag();
		}
		Item_ListUser temItem = arrListUser.get(position);
		holder.textUsername.setText(temItem.getUsername());
		holder.textDate.setText(temItem.getDate());
		if ( !temItem.isByteIcon() ) {
			holder.icon.setImageResource(temItem.getIdIcon());
		}
		return convertView;
	}

	private class HolderView {
		private TextView textUsername, textDate;
		private ImageView icon;
	}

}
