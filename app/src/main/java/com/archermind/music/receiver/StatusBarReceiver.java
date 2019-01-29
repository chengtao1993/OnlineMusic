package com.archermind.music.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.archermind.music.service.AudioPlayer;

/**
 * Created by archermind on 2018/10/30
 */
public class StatusBarReceiver extends BroadcastReceiver {
    public static final String ACTION_STATUS_BAR = "com.archermind.music.STATUS_BAR_ACTIONS";
    public static final String EXTRA = "extra";
    public static final String EXTRA_NEXT = "next";
    public static final String EXTRA_PLAY_PAUSE = "play_pause";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return;
        }

        String extra = intent.getStringExtra(EXTRA);
        if (TextUtils.equals(extra, EXTRA_NEXT)) {
            AudioPlayer.get().next();
        } else if (TextUtils.equals(extra, EXTRA_PLAY_PAUSE)) {
            AudioPlayer.get().playPause();
        }
    }
}
