package com.archermind.music.http;

/**
 * Created by archermind on 2018/10/30.
 */
public abstract class HttpCallback<T> {
    public abstract void onSuccess(T t);

    public abstract void onFail(Exception e);

    public void onFinish() {
    }
}
