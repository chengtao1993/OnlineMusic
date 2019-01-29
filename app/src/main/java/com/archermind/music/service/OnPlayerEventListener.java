package com.archermind.music.service;

import com.archermind.music.model.Music;

/**
 * 播放进度监听器
 * Created by archermind on 2018/10/30
 */
public interface OnPlayerEventListener {

    /**
     * 切换歌曲
     */
    void onChange(Music music);

    /**
     * 继续播放
     */
    void onPlayerStart();

    /**
     * 暂停播放
     */
    void onPlayerPause();

    /**
     * 更新进度
     */
    void onPublish(int progress);

    /**
     * 缓冲百分比
     */
    void onBufferingUpdate(int percent);
}
