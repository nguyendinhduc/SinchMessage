package com.ducnd.democall_message_sinch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.ducnd.common.CommonVL;
import com.ducnd.myinterface.UpdateListView;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class MyService extends Service implements UpdateListView {
    private static final String TAG = "MyService";
    private static String idMine = getIdMine();
    private static String idTo;
    private SinchClient sinchClient;
    private MessageClient messageClient;
    private static ActionMessage actionMessage;
    private static String textMessage;
    private static Bitmap bmMessage;
    private static String idRegister;

    private static final String KEY_REGISTER_GCM = "KEY_REGISTER_GCM";
    private static final String FILE_NAME = "id_registerGCM";


    private BroadMyService broadcastMyService;

    private boolean checkLogout = true;
    @Override
    public void onCreate() {
        super.onCreate();
        registerBroadcast();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            ParseUser parseUser = ParseUser.getCurrentUser();
            if (parseUser != null) idMine = parseUser.getObjectId();
            if (idRegister == null) idRegister = readIdRegister();
            if (idMine != null && sinchClient == null)
                startSinchClient();
        } else {
            String temIdMine = intent.getStringExtra(CommonVL.ID_MINE);
//            idRegister = intent.getStringExtra(CommonVL.REGISTER_ID);
//            if (readIdRegister() == null) writeIdRegisterGCM(idRegister);
            if (!temIdMine.equals(idMine) || sinchClient == null) {
                idMine = temIdMine;
                startSinchClient();
            }
        }

        return START_STICKY;
    }

    private static String getIdMine() {
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null)
            return user.getObjectId();
        else
            return null;
    }

    private void startSinchClient() {
        ListenerMessage listenerMessage = new ListenerMessage();

        if (sinchClient != null ) {
            sinchClient.stopListeningOnActiveConnection();
            sinchClient.terminate();
            sinchClient = null;
        }

        sinchClient = Sinch.getSinchClientBuilder().context(this)
                .userId(idMine).applicationKey(CommonVL.APP_KEY)
                .applicationSecret(CommonVL.APP_SECRET)
                .environmentHost(CommonVL.ENVIRONMENT).build();
        sinchClient.setSupportMessaging(true);
        sinchClient.setSupportActiveConnectionInBackground(true);
        sinchClient.setSupportPushNotifications(true);
        sinchClient.addSinchClientListener(listenerMessage);
//        sinchClient.registerPushNotificationData(idRegister.getBytes());

        sinchClient.checkManifest();
        sinchClient.start();


    }

    private void writeIdRegisterGCM(String idRegisterGCM) {
        SharedPreferences sharedPreferences = this.getSharedPreferences(FILE_NAME, MODE_APPEND);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(KEY_REGISTER_GCM, idRegisterGCM);
        edit.commit();
        edit.apply();
    }

    private String readIdRegister() {
        return this.getSharedPreferences(FILE_NAME, MODE_PRIVATE).getString(KEY_REGISTER_GCM, null);
    }

    @Override
    public String updateText() {
        String tem = textMessage;
        return tem;
    }

    @Override
    public Bitmap updateImage() {
        Bitmap bm = Bitmap.createBitmap(bmMessage);
        return bmMessage;
    }


    private class ListenerMessage implements SinchClientListener, MessageClientListener {

        private static final String TAG = "ListenerMessage";

        @Override
        public void onClientFailed(SinchClient arg0, SinchError arg1) {
            Log.i(TAG, "onClientFailed");
            Toast.makeText(MyService.this, "onClientFailed", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onClientStarted(SinchClient client) {
            Log.i(TAG,"onClientStarted" );
            client.startListeningOnActiveConnection();
//            sinchClient.registerPushNotificationData(idRegister.getBytes());
            messageClient = client.getMessageClient();
            messageClient.addMessageClientListener(this);

        }

        @Override
        public void onClientStopped(SinchClient client) {
            Toast.makeText(MyService.this, "onClientStopped", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLogMessage(int arg0, String arg1, String arg2) {
            Log.i(TAG, "onLogMessage) arg0: " + arg0 + ", arg1: " + arg1 + " , arg2: " + arg2);
            Log.i(TAG, "onLogMessage");
        }

        @Override
        public void onRegistrationCredentialsRequired(SinchClient client,
                                                      ClientRegistration cilentRegistration) {
            Log.i(TAG, "onRegistrationCredentialsRequired");
            Toast.makeText(MyService.this, "onRegistrationCredentialsRequired", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onIncomingMessage(MessageClient messageClient, Message message) {
            String messageText = message.getTextBody();
            String idParse = messageText.substring(messageText.lastIndexOf(' ') + 1, messageText.length());
            String hasFile = messageText.substring(0, messageText.lastIndexOf(' '));
            hasFile = hasFile.substring(hasFile.lastIndexOf(' ') + 1, hasFile.length());
            Intent i = new Intent();
            i.setAction(CommonVL.UPDATE_LISTVIEW);
            if (hasFile.equals(CommonVL.HAS_FILE_FROME)) {
                i.putExtra(CommonVL.FILE_TO, true);
            } else {
                i.putExtra(CommonVL.FILE_TO, false);
            }
            try {
                i.putExtra(CommonVL.BITMAP_MESSAGE, getBitmap(idParse));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            i.putExtra(CommonVL.TEXT_MESSAGE, messageText.substring(0, messageText.indexOf(hasFile) - 1));
            sendBroadcast(i);

        }





        @Override
        public void onMessageDelivered(MessageClient client,
                                       MessageDeliveryInfo messageDeliverInfor) {

        }

        @Override
        public void onMessageFailed(MessageClient client, Message message,
                                    MessageFailureInfo messageFailureInfo) {

        }

        @Override
        public void onMessageSent(MessageClient client, Message message, String content) {

        }

        @Override
        public void onShouldSendPushData(MessageClient client, Message message,
                                         List<PushPair> listPushPair) {
            // TODO Auto-generated method stub
            //get the id that is registered with Sinch
            final String regId = new String(listPushPair.get(0).getPushData());
//use an async task to make the http request
            class SendPushTask extends AsyncTask<Void, Void, Void> {
                @Override
                protected Void doInBackground(Void... voids) {
                    HttpClient httpclient = new DefaultHttpClient();
                    //url of where your backend is hosted, can't be local!
                    HttpPost httppost = new HttpPost("http://your-domain.com?reg_id=" + regId);
                    try {
                        HttpResponse response = httpclient.execute(httppost);
                        ResponseHandler<String> handler = new BasicResponseHandler();
                        Log.d("HttpResponse", handler.handleResponse(response));
                    } catch (ClientProtocolException e) {
                        Log.d("ClientProtocolException", e.toString());
                    } catch (IOException e) {
                        Log.d("IOException", e.toString());
                    }
                    return null;
                }
            }
            
            Log.i(TAG,"onShouldSendPushData" );
            Toast.makeText(MyService.this, "onShouldSendPushData", Toast.LENGTH_SHORT).show();
                    (new SendPushTask()).execute();

        }

    }
    public static byte[] getBitmap(String id) throws ParseException {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        query.whereEqualTo("objectId", id);

        ParseObject parseObject = query.find().get(0);
        if (parseObject == null) {
            return null;
        }
        ParseFile fileParse = (ParseFile) parseObject.get("file");
        if (fileParse != null) {
            byte b[] = fileParse.getData();
            return b;
        }

        return null;
    }

    private synchronized void sendMessageNotFile() {
        Log.i(TAG, "sendMessageNotFile");
        final ParseObject parseObject = new ParseObject(CommonVL.PARSE_MESSAGE);
        parseObject.put("idFrom", idMine);
        parseObject.put("idTo", idTo);
        parseObject.put("hasFile", false);
        parseObject.put("messageText", actionMessage.getBodyMessage());
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i(TAG, "saveInBackground_done_sendMessageNotFile");
                    WritableMessage message = new WritableMessage(idTo, actionMessage.getBodyMessage() + " " +
                            CommonVL.NOT_FILE_FROME + " " + parseObject.getObjectId());
                    messageClient.send(message);

                }
                checkLogout = true;
            }
        });


    }

    private void sendMessageHasFile() {
        try {
            final ParseFile parseFile = getParseFile(actionMessage.getUriFile());
            final ParseObject parseObject = new ParseObject(CommonVL.PARSE_MESSAGE);
            parseObject.put("idFrom", idMine);
            parseObject.put("idTo", idTo);
            parseObject.put("file", parseFile);
            parseObject.put("hasFile", true);
            parseObject.put("messageText", actionMessage.getBodyMessage());
            parseObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        WritableMessage message = new WritableMessage(idTo, actionMessage.getBodyMessage() + " " +
                                CommonVL.HAS_FILE_FROME + " " + parseObject.getObjectId());
                        messageClient.send(message);
                    } else {
                        Log.i(TAG, "ERROR: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkLogout = true;
    }

    private ParseFile getParseFile(Uri uriImage) throws IOException {

        if (uriImage == null) {
            return null;
        }
        Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uriImage);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] arrByte = stream.toByteArray();
        ParseFile parseFile = null;
        if (arrByte != null) {
            parseFile = new ParseFile(arrByte);
        }
        stream.close();
        return parseFile;
    }

    public static void setActionMessage(ActionMessage actionMessage) {
        MyService.actionMessage = actionMessage;
    }

    public interface ActionMessage {
        public String getBodyMessage();

        public Uri getUriFile();
    }

    private void registerBroadcast() {
        if (broadcastMyService != null) return;
        broadcastMyService = new BroadMyService();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonVL.ACTION_SEND_MESSAGE);
        filter.addAction(CommonVL.LOGOUT);
        registerReceiver(broadcastMyService, filter);
    }

    private class BroadMyService extends BroadcastReceiver {
        private static final String TAG = "BroadMyService";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CommonVL.ACTION_SEND_MESSAGE)) {
               if ( sinchClient != null && sinchClient.isStarted()) {
                   checkLogout = false;
                   MyService.idTo = intent.getStringExtra(CommonVL.ID_TO);
                   if (intent.getBooleanExtra(CommonVL.FILE_FROM, false)) sendMessageHasFile();
                   else sendMessageNotFile();
               }
                else {
                    Toast.makeText(MyService.this, "ERROR", Toast.LENGTH_SHORT).show();
               }
            }

            if (intent.getAction().equals(CommonVL.LOGOUT)) {
               while (!checkLogout) {
                   SystemClock.sleep(100);
               }
                if ( sinchClient != null ) {
                    sinchClient.stopListeningOnActiveConnection();
                    sinchClient.terminate();
                    sinchClient = null;
                }
            }

        }
    }

}
