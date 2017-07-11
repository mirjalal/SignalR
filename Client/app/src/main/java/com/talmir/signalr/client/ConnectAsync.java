package com.talmir.signalr.client;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;

import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler2;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.support.design.widget.Snackbar.make;

final class ConnectAsync extends AsyncTask<Void, String, String>
{
    private Context context;

    ConnectAsync(Context context)
    {
        this.context = context;
    }

    /**
     * Void: the type of doInBackground()'s and execute() var-args
     * parameters.
     *
     * String: the type of publishProgress()'s and onProgressUpdate()'s
     * var-args parameters.
     *
     * String: the type of doInBackground()'s return value, onPostExecute()'s
     * parameter, onCancelled()'s parameter, and get()'s return value.
     *
     *
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p/>
     * This method can call {@link #publishProgress} to publish updates
     * on the com.kiber.autodrome.client.UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    @Nullable
    protected String doInBackground(Void... params) {
        Logger logger = new Logger() {
            /**
             * Logs a message
             *
             * @param message Message to log
             * @param level   Level of #message
             */
            @Override
            public void log(String message, LogLevel level) {
                Log.e("LOGGER MESSAGE", " " + message);
            }
        };
        try {
            ChatActivity.connection = new HubConnection(ChatActivity.serverURI, "", true, logger);
        } catch (Exception ignored) {
            if (!isCancelled()) {
                try {
                    cancel(true);
                    make(ChatActivity.relativeView, "Couldn't connect to server.", LENGTH_LONG)
                            .setAction("Retry", v -> {
                                // Check if AsyncTask is cancelled
                                if (!isCancelled()) {
                                    // cancel task
                                    cancel(true);
                                    // Check again to be sure task was cancelled.
                                    if (isCancelled())
                                        // If canceled, run task again. This will repeat until
                                        // user stops clicking the "Retry" button on Snackbar.
                                        new ConnectAsync(context).execute();
                                } else
                                    new ConnectAsync(context).execute();
                            })
                            .show();
                } catch (Exception e) {

                }
            }
        }
//            ChatActivity.connection.closed(new Runnable() {
//                @Override
//                public void run() {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            send.setEnabled(false);
//                            make(ChatActivity.relativeView, "Couldn't connect to server.", LENGTH_LONG).show();
//                        }
//                    });
//                }
//            });

        // look to C# WinFormsServer server application for name "ChatHub"
        ChatActivity.hubProxy = ChatActivity.connection.createHubProxy("ChatHub");
        // look to C# WinFormsServer server application for name "AddMessage"
        ChatActivity.hubProxy.on(
                "AddMessage",
                new SubscriptionHandler2<String, String>() {
                    @Override
                    public synchronized void run(String user, String message) {
                        publishProgress(user + ": " + message);
                    }
                },
                String.class,
                String.class
        );

        SignalRFuture awaitConnection = ChatActivity.connection.start(new ServerSentEventsTransport(ChatActivity.connection.getLogger()));
        try {
            // You may simplify the line below by removing parameters below
            awaitConnection.get();
        } catch (Exception e) {
            Log.wtf("E R R O R ", e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result)
    {
        new Handler().postDelayed(() -> {
            // Handle the Snackbar messages
            if (ChatActivity.connection.getState() == ConnectionState.Connected)
            {
                make(ChatActivity.relativeView, "Connected!", LENGTH_LONG).show();
                new UI();
            }
            else if (ChatActivity.connection.getState() == ConnectionState.Connecting ||
                    ChatActivity.connection.getState() == ConnectionState.Reconnecting)
                make(ChatActivity.relativeView, "Trying to reconnect server.", LENGTH_LONG).show();
            else
                make(ChatActivity.relativeView, "Couldn't connect to server.", LENGTH_LONG)
                        .setAction("Retry", v -> {
                            // Check if task is cancelled
                            if (!isCancelled()) {
                                // cancel task
                                cancel(true);
                                // Check again to be sure task was cancelled.
                                if (isCancelled())
                                    // If canceled, run task again. This will repeat until
                                    // user stops clicking the "Retry" button on Snackbar.
                                    new ConnectAsync(context).execute();
                            } else
                                new ConnectAsync(context).execute();
                        })
                        .show();
        }, 1000);
    }

    @Override
    protected void onProgressUpdate(String... params)
    {
        // Update UI
        ChatActivity._ui.renderView(context, Arrays.toString(params));
    }
}
