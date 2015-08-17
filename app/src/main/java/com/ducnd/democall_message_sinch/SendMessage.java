package com.ducnd.democall_message_sinch;


import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ducnd.common.CommonVL;
import com.ducnd.democall_message_sinch.MyService.ActionMessage;
import com.ducnd.myadapter.AdapterListMessage;
import com.ducnd.myadapter.MyAdaper_ListUser;
import com.ducnd.myinterface.QueryUser;
import com.ducnd.myinterface.UpdateListView;
import com.ducnd.myitem.ItemListMessage;
import com.ducnd.myitem.Item_ListUser;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SendMessage extends Activity implements View.OnClickListener, ActionMessage, SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {
    private static final String TAG = "SendMessage";
    private MyDialogLisetUser dialogSendMessage;
    private EditText editText;
    private Button btnSend, btnAttach, btnCancelAttach;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static final int SELECT_IMAGE = 23423;
    private boolean isAttach = false;
    private Uri uriAttach = null;
    private static UpdateListView updateListView;
    private BroadcastSendMessage broadcastSendMessage;
    private ReentrantLock mLock = new ReentrantLock();
    private ListView listMessage;
    private AdapterListMessage adapterListMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_send_message);
        registerBroadSendMessage();
        init();
        updateHistory();
    }

    private void init() {
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        btnAttach = (Button) findViewById(R.id.btnAttach);
        btnAttach.setOnClickListener(this);
        btnCancelAttach = (Button) findViewById(R.id.btnCancelAttach);
        btnCancelAttach.setOnClickListener(this);
        editText = (EditText) findViewById(R.id.edittext);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);

        listMessage = (ListView) findViewById(R.id.listMessage);
        adapterListMessage = new AdapterListMessage(this);
        listMessage.setAdapter(adapterListMessage);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSend:
                showListUser();
                break;
            case R.id.btnAttach:
                showGallary();
                break;
            case R.id.btnCancelAttach:
                isAttach = false;
                break;

            default:
                break;
        }
    }

    private void showListUser() {
        Log.i(TAG, "getDataUser exist");
        ParseUser parseCurrent = ParseUser.getCurrentUser();

        String idCurrent = parseCurrent.getObjectId();
        ParseQuery<ParseUser> query = ParseUser.getQuery();

        query.whereNotEqualTo("objectId", idCurrent);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, com.parse.ParseException e) {
                if (e != null) {
                    Toast.makeText(getBaseContext(), "ERROR load user",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                ArrayList<Item_ListUser> arrListuser = new ArrayList<Item_ListUser>();
                for (ParseUser i : list) {
                    String name = i.getString("username");
                    java.util.Date date = i.getCreatedAt();

                    String at = "";
                    if (date != null) {
                        SimpleDateFormat sim = new SimpleDateFormat(
                                "dd:MM:yyyy hh:mm");
                        at = sim.format(date);
                        Log.i(TAG, "date not null ");
                    }
                    arrListuser
                            .add(new Item_ListUser(i.getObjectId(), name, at, R.drawable.user));
                    Log.i(TAG, "findInBackground_name: " + name);
                    Log.i(TAG, "findInBackground_at: " + at);
                }

                Log.i(TAG, "findInBackground_finish....");
                dialogSendMessage = new MyDialogLisetUser(SendMessage.this);
                dialogSendMessage.setArrUser(arrListuser);
                dialogSendMessage.show();
                Log.i(TAG, "findInBackground_size arr: " + arrListuser.size());
            }
        });
        Log.i(TAG, "findInBackground_finish");

    }

    private void showGallary() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                uriAttach = data.getData();
                isAttach = true;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    private class MyDialogLisetUser extends Dialog implements
            android.view.View.OnClickListener, AdapterView.OnItemClickListener {

        private static final String TAG = "MyDialogLisetUser";
        private Button btnOk;
        private ListView listView;
        private ArrayList<Item_ListUser> arrListUser = new ArrayList<Item_ListUser>();
        private MyAdaper_ListUser adapter;
        private QueryUser queryUser;

        public MyDialogLisetUser(Context context, boolean cancelable,
                                 OnCancelListener cancelListener) {
            super(context, cancelable, cancelListener);
        }

        public MyDialogLisetUser(Context context, int theme) {
            super(context, theme);
        }

        public MyDialogLisetUser(Context context) {
            super(context);
        }

        public void setArrUser(ArrayList<Item_ListUser> arrListUser) {
            this.arrListUser = arrListUser;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_show_listuser);
            registerBroadSendMessage();
            init();
        }

        public void init() {
            listView = (ListView) findViewById(R.id.listview);


            Log.i(TAG, "init_size arr: " + arrListUser.size());
            adapter = new MyAdaper_ListUser(getContext(), arrListUser);

            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);

            btnOk = (Button) findViewById(R.id.btnOk);
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

        @Override
        public void onItemClick(AdapterView<?> arr, View view, int position,
                                long id) {
            Intent intent = new Intent(CommonVL.ACTION_SEND_MESSAGE);
            if (SendMessage.this.isAttach) {
                intent.putExtra(CommonVL.FILE_FROM, true);
            } else {
                intent.putExtra(CommonVL.FILE_FROM, false);
            }
            intent.putExtra(CommonVL.ID_TO, arrListUser.get(position).getIdParse());
            MyService.setActionMessage(SendMessage.this);
            sendBroadcast(intent);
            Log.i(TAG, "onItemClick");
        }

    }

    @Override
    public String getBodyMessage() {
        return editText.getText().toString();
    }

    @Override
    public Uri getUriFile() {
        if (this.isAttach)
            return this.uriAttach;
        else
            return null;
    }

    public static void udateListView(UpdateListView updateListView) {
        SendMessage.updateListView = updateListView;
    }

    private void startService() {
        Intent intent = new Intent();
        intent.setClassName("com.ducnd.democall_message_sinch",
                "com.ducnd.democall_message_sinch.MyService");
        intent.putExtra(CommonVL.ID_MINE, ParseUser.getCurrentUser()
                .getObjectId());
        startService(intent);

    }

    private void registerBroadSendMessage() {
        if (broadcastSendMessage == null) {
            Log.i(TAG, "registerBroadSendMessage");
            broadcastSendMessage = new BroadcastSendMessage();
            IntentFilter filter = new IntentFilter();
            filter.addAction(CommonVL.UPDATE_LISTVIEW);
            registerReceiver(broadcastSendMessage, filter);
        }
    }

    private void unregisterBroadSendMessage() {
        if (broadcastSendMessage != null) {
            Log.i(TAG,"unregisterBroadSendMessage" );
            unregisterReceiver(broadcastSendMessage);

            broadcastSendMessage = null;

        }
    }

    private class BroadcastSendMessage extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"onReceive" );
            if (intent.getAction().equals(CommonVL.UPDATE_LISTVIEW)) {
                Log.i(TAG,"onReceive" );
                String text = intent.getStringExtra(CommonVL.TEXT_MESSAGE);
                mLock.lock();
                if (intent.getBooleanExtra(CommonVL.FILE_TO, false)) {
                    byte[] b = intent.getByteArrayExtra(CommonVL.BITMAP_MESSAGE);
                    Bitmap bm = BitmapFactory.decodeByteArray(b, 0, b.length);
                    adapterListMessage.addItem(text, bm);
                } else {
                    adapterListMessage.addItem(text);
                }
                listMessage.setSelection(adapterListMessage.getCount()-1);
                mLock.unlock();
            }
        }
    }

    private void updateHistory() {
        ArrayList<ItemListMessage> messagesHistory = new ArrayList<ItemListMessage>();
        ParseUser user = ParseUser.getCurrentUser();

        if (user == null ) return;
        new QueryHistory(user.getObjectId()).execute();
    }

    private class QueryHistory extends AsyncTask<Void, ItemListMessage, Void> {
        private String idUser;
        public QueryHistory ( String idUser ) {
            this.idUser = idUser;
        }
        @Override
        protected Void doInBackground(Void... params) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery(CommonVL.PARSE_MESSAGE);
            query.whereEqualTo("idFrom", idUser);
            query.setLimit(1000);
            query.orderByDescending("createdAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    for (ParseObject i : list) {
                        String messageText = i.getString("messageText");

                        if (i.getBoolean("hasFile")) {
                            try {
                                byte b[] = ((ParseFile)i.get("file")).getData();
                                Bitmap bm = BitmapFactory.decodeByteArray(b, 0, b.length);
                                publishProgress(new ItemListMessage(messageText, bm));
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }

                        } else {
                            publishProgress( new ItemListMessage(messageText));
                        }

                    }
                }
            });

            return null;
        }

        @Override
        protected synchronized void onProgressUpdate(ItemListMessage... values) {
            adapterListMessage.addItem(0, values[0]);
            listMessage.setSelection(adapterListMessage.getCount()-1);
            super.onProgressUpdate(values);
        }
    }

    @Override
    public void onBackPressed() {
        unregisterBroadSendMessage();
        super.onBackPressed();
    }
    @Override
    protected void onDestroy() {
        unregisterBroadSendMessage();
        super.onDestroy();
    }


}
