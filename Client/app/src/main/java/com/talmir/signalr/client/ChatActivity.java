package com.talmir.signalr.client;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.support.design.widget.Snackbar.make;

public class ChatActivity extends AppCompatActivity {
    // local IPv4 Address of the server (computer)
    protected static final String serverURI = "http://192.168.0.106:27"; // signalr hissesini yigisdir

    protected static String userName;
    protected static EditText usernameEditText;
    protected static Button send, sign_In;
    protected static HubProxy hubProxy;
    protected static HubConnection connection;
    protected static View relativeView;
    protected static ScrollView scrollView;
    protected static LinearLayout linearLayout;
    protected static boolean isReceived = true;
    protected static UI _ui;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // load SignalR library components here
        Platform.loadPlatformComponent(new AndroidPlatformComponent());

        relativeView = findViewById(R.id.view);
        sign_In = (Button) findViewById(R.id.signIn);
        send = (Button) findViewById(R.id.sendButton);
        send.setEnabled(false);
        scrollView = (ScrollView) findViewById(R.id.SCROLLER_ID);
        linearLayout = (LinearLayout) findViewById(R.id.messages);
        usernameEditText = (EditText) findViewById(R.id._username);
        relativeView.setBackgroundColor(Color.rgb(233, 237, 239));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.signIn:
                        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                        // Connected to the internet
                        if (activeNetwork != null) {
                            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI ||
                                    activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                                // Connected to wifi or mobile provider's data plan.
                                // Do your work here...
                                userName = usernameEditText.getText().toString().trim();
                                if (userName.length() > 0) {
                                    sign_In.setEnabled(false);
//                                    _username.setEnabled(false);
                                    usernameEditText.setEnabled(false);
                                    make(relativeView, "Connecting to server...", LENGTH_LONG).show();
                                    new ConnectAsync(getApplicationContext()).execute();
                                    _ui = new UI();
                                }
                            }
                        } else
                            make(relativeView, "Connect device to network.", LENGTH_LONG).show();
                        break;
                    case R.id.sendButton:
                        String text = usernameEditText.getText().toString().trim();
                        if (text.length() > 0) {
                            isReceived = false;
                            hubProxy.invoke("Send", userName, text);
                            _ui.renderView(getApplicationContext(), text);
                            usernameEditText.setText("");
                            usernameEditText.requestFocus();
                        }
                        break;
                }
            }
        };
        sign_In.setOnClickListener(listener);
        send.setOnClickListener(listener);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("Exit", (arg0, arg1) -> {
                    // Disconnect connection before quiting from app.
                    if (connection != null)
                        connection.disconnect();
                    ChatActivity.super.onBackPressed();
                })
                .create()
                .show();
    }
}
