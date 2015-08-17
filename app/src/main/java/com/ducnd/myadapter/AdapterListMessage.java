package com.ducnd.myadapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ducnd.democall_message_sinch.R;
import com.ducnd.myitem.ItemListMessage;

import java.util.ArrayList;

public class AdapterListMessage extends BaseAdapter {
    private ArrayList<ItemListMessage> messages;
    private Context context;
    private LayoutInflater inflater;

    public AdapterListMessage(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(this.context);
        this.messages = new ArrayList<ItemListMessage>();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public ItemListMessage getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.item_list_message, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.text.setText(messages.get(position).getText());
        viewHolder.image.setImageBitmap(messages.get(position).getImage());
        return convertView;
    }

    private class ViewHolder {
        private ImageView image;
        private TextView text;

    }

    public void addItem(ItemListMessage item) {
        messages.add(item);
        notifyDataSetChanged();
    }
    public void addItem( int position, ItemListMessage item ) {
        messages.add(position, item);
        notifyDataSetChanged();
    }

    public void addItem(String text, Bitmap image) {
        messages.add(new ItemListMessage(text, image));
        notifyDataSetChanged();
    }

    public void addItem(int postion, String text, Bitmap image) {
        messages.add(postion, new ItemListMessage(text, image));
        notifyDataSetChanged();
    }

    public void addItem(String text) {
        messages.add(new ItemListMessage(text));
        notifyDataSetChanged();
    }
    public void addItem(int position, String text) {
        messages.add(position, new ItemListMessage(text));
        notifyDataSetChanged();
    }



}
