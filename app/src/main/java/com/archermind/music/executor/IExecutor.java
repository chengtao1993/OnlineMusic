package com.archermind.music.executor;

/**
 * Created by archermind on 2018/10/30
 */
public interface IExecutor<T> {
    void execute();

    void onPrepare();

    void onExecuteSuccess(T t);

    void onExecuteFail(Exception e);
}
