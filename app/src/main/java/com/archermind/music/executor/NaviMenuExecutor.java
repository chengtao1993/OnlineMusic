package com.archermind.music.executor;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import com.archermind.music.R;
import com.archermind.music.activity.AboutActivity;
import com.archermind.music.activity.MusicActivity;
import com.archermind.music.activity.SettingActivity;
import com.archermind.music.constants.Actions;
import com.archermind.music.service.PlayService;
import com.archermind.music.service.QuitTimer;
import com.archermind.music.storage.preference.Preferences;
import com.archermind.music.utils.ToastUtils;

/**
 * 导航菜单执行器
 * Created by archermind on 2018/10/30.
 */
public class NaviMenuExecutor {
    private MusicActivity activity;

    public NaviMenuExecutor(MusicActivity activity) {
        this.activity = activity;
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                startActivity(SettingActivity.class);
                return true;
            case R.id.action_night:
                nightMode();
                break;
            case R.id.action_timer:
                timerDialog();
                return true;
            case R.id.action_exit:
                activity.finish();
                PlayService.startCommand(activity, Actions.ACTION_STOP);
                return true;
            case R.id.action_about:
                startActivity(AboutActivity.class);
                return true;
        }
        return false;
    }

    private void startActivity(Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        activity.startActivity(intent);
    }

    private void nightMode() {
        Preferences.saveNightMode(!Preferences.isNightMode());
        activity.recreate();
    }

    private void timerDialog() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.menu_timer)
                .setItems(activity.getResources().getStringArray(R.array.timer_text), (dialog, which) -> {
                    int[] times = activity.getResources().getIntArray(R.array.timer_int);
                    startTimer(times[which]);
                })
                .show();
    }

    private void startTimer(int minute) {
        QuitTimer.get().start(minute * 60 * 1000);
        if (minute > 0) {
            ToastUtils.show(activity.getString(R.string.timer_set, String.valueOf(minute)));
        } else {
            ToastUtils.show(R.string.timer_cancel);
        }
    }
}
