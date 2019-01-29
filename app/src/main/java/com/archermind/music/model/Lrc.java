package com.archermind.music.model;

import com.google.gson.annotations.SerializedName;

/**
 * JavaBean
 * Created by archermind on 2018/10/30
 */
public class Lrc {
    @SerializedName("lrcContent")
    private String lrcContent;

    public String getLrcContent() {
        return lrcContent;
    }

    public void setLrcContent(String lrcContent) {
        this.lrcContent = lrcContent;
    }
}
