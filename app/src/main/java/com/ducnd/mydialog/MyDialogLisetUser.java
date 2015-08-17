package com.ducnd.mydialog;

import java.util.ArrayList;

import com.ducnd.democall_message_sinch.R;
import com.ducnd.myadapter.MyAdaper_ListUser;
import com.ducnd.myinterface.QueryUser;
import com.ducnd.myitem.Item_ListUser;

import android.app.Dialog;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MyDialogLisetUser extends Dialog implements android.view.View.OnClickListener{
	
	private static final String TAG = "MyDialogLisetUser";
	private Button btnOk;
	private ListView listView;
	private ArrayList<Item_ListUser> arrListUser = new ArrayList<Item_ListUser>();
	private MyAdaper_ListUser adapter;
	private QueryUser queryUser;

	public MyDialogLisetUser(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	public MyDialogLisetUser(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	public MyDialogLisetUser(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void setArrUser( ArrayList<Item_ListUser> arrListUser ) {
		this.arrListUser = arrListUser;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_show_listuser);
		init();
	}
	public void init() {
		listView = (ListView)findViewById(R.id.listview);
		
		Log.i(TAG, "init_size arr: " + arrListUser.size());
		adapter = new MyAdaper_ListUser(getContext(), arrListUser);
	
		listView.setAdapter(adapter);
		
		btnOk = (Button)findViewById(R.id.btnOk);
		btnOk.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnOk:
			dismiss();
			break;

		default:
			break;
		}
	}

}
