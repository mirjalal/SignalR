package com.talmir.signalr.client;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

final class UI
{
    UI()
    {
        ChatActivity.sign_In.setVisibility(View.GONE);
        ChatActivity.send.setEnabled(true);
        ChatActivity.usernameEditText.setHint(R.string.enterText);
        ChatActivity.usernameEditText.setEnabled(true);
        ChatActivity.usernameEditText.setText("");
    }

    private void scroll()
    {
        new Handler().post(() -> ChatActivity.scrollView.scrollTo(0, ChatActivity.linearLayout.getBottom()));
    }

    void renderView(final Context context, String message)
    {
        TextView chat_message = new TextView(context);
        chat_message.setBackgroundResource(R.drawable.corners); // set corners & padding
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 5, 0, 5);
        GradientDrawable drawable = (GradientDrawable) chat_message.getBackground();
        if (ChatActivity.isReceived)
        {
            lp.gravity = Gravity.END;
            drawable.setColor(ContextCompat.getColor(context, R.color.white));
            chat_message.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            chat_message.append(
                    message                                 // get single-line string from string array
                            .substring(                     // take substring from string
                                    1,                      // extract required string from
                                    message                 // get single-line string from string array again to get it's length
                                            .trim()         // trim again
                                            .length() - 1   // finish
                            )
            );
        }
        else
        {
            lp.gravity = Gravity.START;
            drawable.setColor(ContextCompat.getColor(context, R.color.colorAccent));
            chat_message.setTextColor(ContextCompat.getColor(context, R.color.white));
            chat_message.append(message);
        }

        chat_message.setLayoutParams(lp);
        ChatActivity.linearLayout.addView(chat_message);
        ChatActivity.isReceived = true;
        scroll();
    }
}