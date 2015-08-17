package com.ducnd.democall_message_sinch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ducnd.common.CommonVL;
import com.ducnd.mydialog.MyDialogLisetUser;
import com.ducnd.myinterface.QueryUser;
import com.ducnd.myitem.Item_ListUser;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class Login_Signup_Activity extends Activity implements OnClickListener {
    protected static final String TAG = "Login_Signup_Activity";
    protected static final int SHOW_LIST_USER = 345335;
    private Button btnLogin, btnSignup, btnCheck, btnLogout;
    private EditText editUsername, editPassword, editEmail;
    private String username, password, email;
    private MyDialogLisetUser dialogListUser;
    public static int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_longin_signup);
        init();
    }

    public void init() {
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        btnSignup = (Button) findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(this);
        btnCheck = (Button) findViewById(R.id.btnCheck);
        btnCheck.setOnClickListener(this);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);
        editPassword = (EditText) findViewById(R.id.editPassword);
        editUsername = (EditText) findViewById(R.id.editUsername);
        editEmail = (EditText) findViewById(R.id.editEmail);

    }

    @Override
    public void onClick(View v) {
        username = editUsername.getText().toString();
        password = editPassword.getText().toString();
        email = editEmail.getText().toString();

        switch (v.getId()) {
            case R.id.btnLogin:
                Toast.makeText(this, "btnLogin", Toast.LENGTH_SHORT).show();
                actionLogin();
                break;
            case R.id.btnSignup:
                actionSignup();
                break;
            case R.id.btnCheck:
//                sendEmailResetPassword(editEmail.getText().toString());
                break;
            case R.id.btnLogout:
                Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
                Intent i = new Intent();
                i.setAction(CommonVL.LOGOUT);
                sendBroadcast(i);
                ParseUser.logOut();
            default:
                break;
        }

    }


    private void actionLogin() {
        if (!checkNullUsernameAndPasswordAndEmail()) {
            Toast.makeText(this, "Please input engough information",
                    Toast.LENGTH_SHORT).show();

            return;
        }
        ParseUser.logInInBackground(username, password, new LogInCallback() {

            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    Log.i(TAG, "actionLogin: " + "Login succuss");
                    new RegisterGcmTask().execute();
                } else {
                    Log.i(TAG, "actionLogin: " + "Login not success");
                }
            }
        });
    }

    private void actionSignup() {
        if (!checkNullUsernameAndPasswordAndEmail()) {
            Toast.makeText(this, "Please input engough information",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ParseUser parseUser = new ParseUser();
        parseUser.setUsername(username);
        parseUser.setPassword(password);
        parseUser.setEmail(email);

        parseUser.signUpInBackground(new SignUpCallback() {

            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i(TAG, "actionSignup_ Sign up  success");
                    Toast.makeText(getBaseContext(), "Sign up success",
                            Toast.LENGTH_SHORT).show();
                    new RegisterGcmTask().execute();
                } else {
                    Log.i(TAG, "actionSignup_ Sign up  not success");
                    e.printStackTrace();
                }

            }
        });

    }


    private class RegisterGcmTask extends AsyncTask<Void, Void, String> {
        final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(Login_Signup_Activity.this);

        String msg = "";

        @Override
        protected String doInBackground(Void... voids) {
            try {
                if (gcm == null) {
                    Toast.makeText(Login_Signup_Activity.this, "NULL", Toast.LENGTH_SHORT).show();
                } else {
                    msg = gcm.register(CommonVL.ID_PROJECT);
                    Log.i(TAG, "RegisterGcmTask_id: " + msg);
                }

            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
                Log.i(TAG, "RegisterGcmTask_id: " + msg);
                ex.printStackTrace();
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {
            Intent intent = new Intent(getApplicationContext(), SendMessage.class);
            Intent serviceIntent = new Intent(getApplicationContext(), MyService.class);
            //serviceIntent.putExtra(CommonVL.REGISTER_ID, msg);
            serviceIntent.putExtra(CommonVL.ID_MINE, ParseUser.getCurrentUser().getObjectId());
            startActivity(intent);
            startService(serviceIntent);
        }
    }

    private boolean checkNullUsernameAndPasswordAndEmail() {
        if (username.equals("") || password.equals("") || email.equals(""))
            return false;
        else
            return true;
    }

    private boolean checkExistCurrenUser() {
        if (ParseUser.getCurrentUser() == null)
            return false;
        else
            return true;
    }

    private void startActivitySendMessage() {
        Intent intent = new Intent();
        intent.setClass(getBaseContext(), SendMessage.class);
        startActivity(intent);
    }

    public void search(final String id) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        // query = query.whereContains("username", "nguyen van d");

        query.getInBackground(id, new GetCallback<ParseUser>() {

            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    Log.i(TAG, "search_" + id + ": true");
                } else {
                    Log.i(TAG, "search_" + id + ": false");
                    Log.i(TAG, "search_exeption: " + e.toString());
                }

            }
        });
    }

    public void searchContaint(String username) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContains("username", username);
        query.findInBackground(new FindCallback<ParseUser>() {

            @Override
            public void done(List<ParseUser> listUser, ParseException e) {
                if (e == null) {
                    for (ParseUser i : listUser) {
                        Log.i(TAG, "usesername: " + i.getString("username"));
                    }
                } else {
                    Log.i(TAG, "findInBackground: not succuss");
                }

            }
        });

    }

    public void putAvatar(String link) {
        File file = new File(link);

        if (!file.exists()) {
            return;
        }

        Bitmap bm = BitmapFactory.decodeFile(link);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] arrByte = stream.toByteArray();
        if (arrByte != null) {
            ParseFile parseFile = new ParseFile(arrByte);
            ParseUser user = ParseUser.getCurrentUser();
            if (user != null) {
                user.put("avatar", parseFile);
                user.put("hello", "hello");

                try {
                    user.save();
                } catch (ParseException e) {
                    Log.i(TAG, "Error: " + e.toString());
                }
                user.saveInBackground();
                Log.i(TAG, "putAvatar_save susse");
            }

        }

    }

    private Bitmap bmGetAvatar;

    public Bitmap getAvatar() {
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) {
            Log.i(TAG, "getAvatar_user null");
            return null;
        }
        Log.i(TAG, "getAvatar_user not null");
        bmGetAvatar = null;
        ParseFile fileParse = (ParseFile) user.get("avatar");
        if (fileParse != null) {
            fileParse.getDataInBackground(new GetDataCallback() {

                @Override
                public void done(byte[] arg0, ParseException arg1) {
                    if (arg1 == null) {
                        bmGetAvatar = BitmapFactory.decodeByteArray(arg0, 0,
                                arg0.length);
                        Log.i(TAG, "getAvatar_success");
                        ((ImageView) findViewById(R.id.image))
                                .setImageBitmap(bmGetAvatar);
                    } else {
                        Log.i(TAG, "getAvatar_notsuccess");
                    }

                }
            });
        }

        return bmGetAvatar;
    }

    public List<ParseUser> getListStartWith(String content, String field) {
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null)
            return null;
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereStartsWith(field, content);
        try {
            return query.find();
        } catch (ParseException e) {
            Log.i(TAG, "getListStartWith_ERROR: " + e.toString());
            return null;
        }
    }

    public List<ParseUser> getSeclectKey(ArrayList<String> fields) {
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null)
            return null;

        ParseQuery<ParseUser> query = user.getQuery();

        query.selectKeys(fields);
        try {
            return query.find();
        } catch (ParseException e) {
            Log.i(TAG, "getSeclectKey_null");
            return null;

        }
    }

    public void storeData() {
        final ParseUser user = ParseUser.getCurrentUser();
        Log.d(TAG, user.getObjectId());
        ParseQuery<ParseUser> query = user.getQuery();
        query.whereEqualTo("username", "4");
        query.findInBackground(new FindCallback<ParseUser>() {

            @Override
            public void done(List<ParseUser> user1, ParseException e) {
                if (e == null) {
                    Log.i(TAG, "ksdjfhks");
                    Log.i(TAG, "size: " + user1.size());
                    // ArrayList<String> arr;
                    // arr = (ArrayList<String>)(user.get("listFriend"));
                    // if ( arr == null ) arr = new ArrayList<String>();
                    // arr.add(user1.get(0).getObjectId());
                    // user.put("listFriend", arr);
                    // user.saveInBackground();

                    ArrayList<ParseUser> arr;
                    arr = (ArrayList<ParseUser>) user.get("listParse");
                    if (arr == null)
                        arr = new ArrayList<ParseUser>();
                    arr.add(user1.get(0));
                    user.put("listparse", arr);
                    user.pinInBackground();
                    // user.saveInBackground();

                    Log.i(TAG, "findInBackground");
                } else {
                    Log.i(TAG, "not findInBackground");
                    Log.i(TAG, e.toString());
                }

            }
        });
    }

    public void getUserStore() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("usename",
                ParseUser.getCurrentUser().getString("username"));
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseUser>() {

            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (e == null) {
                    Log.i(TAG, "size: " + list.size());
                    Log.i(TAG, "success");
                }

            }

        });
    }

    public void getData() {

    }

    public void getDate() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByDescending("createdAt");

        Log.i(TAG, "creat at..............");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (list != null) {
                    for (ParseUser i : list) {
                        java.util.Date d = i.getCreatedAt();
                        Log.i(TAG, "Date: " + d.getTime());
                    }
                } else {
                    Log.i(TAG, "list == null");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void sendEmailResetPassword(String email) {
        ParseUser.requestPasswordResetInBackground(email,
                new RequestPasswordResetCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(Login_Signup_Activity.this, "send success", Toast.LENGTH_SHORT).show();
                            // An email was successfully sent with reset instructions.
                        } else {
                            Toast.makeText(Login_Signup_Activity.this, "send not success", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "Error: " + e.toString());
                            // Something went wrong. Look at the ParseException to see what's up.
                        }
                    }
                });
    }

    //khong duoc
    public void sendMail() {
        Map<String, String> params = new HashMap<>();
        params.put("text", "Sample mail body");
        params.put("subject", "Test Parse Push");
        params.put("fromEmail", "uet.nguyenduc@gmail.com");
        params.put("fromName", "Nguyen dinh duc");
        params.put("toEmail", "uet.nguyendinhduc@gmail.com");
        params.put("toName", "Hello nguyen dinh duc");
        ParseCloud.callFunctionInBackground("sendMail", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
                    Toast.makeText(Login_Signup_Activity.this, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Login_Signup_Activity.this, "not Success, try again", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Error callFunctionInBackground: " + e.toString());
                }

            }
        });
    }

}
