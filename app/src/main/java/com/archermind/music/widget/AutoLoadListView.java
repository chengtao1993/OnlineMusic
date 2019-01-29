package com.archermind.music.widget;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.archermind.music.R;

/**
 * 自动加载更多ListView
 * Created by archermind on 2018/10/30.
 */
public class AutoLoadListView extends RecyclerView {
    private static final String TAG = AutoLoadListView.class.getSimpleName();
    private View vFooter;
    private OnLoadListener mListener;
    private int mFirstVisibleItem = 0;
    private boolean mEnableLoad = true;
    private boolean mIsLoading = false;

    public AutoLoadListView(Context context) {
        super(context);
        init();
    }

    public AutoLoadListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoLoadListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        vFooter = LayoutInflater.from(getContext()).inflate(R.layout.auto_load_list_view_footer, null);
      //  addFooterView(vFooter, null, false);
        onLoadComplete();
    }

    public void setOnLoadListener(OnLoadListener listener) {
        mListener = listener;
    }

    public void onLoadComplete() {
        mIsLoading = false;
       // removeFooterView(vFooter);
    }

    public void setEnable(boolean enable) {
        mEnableLoad = enable;
    }


    @Override
    public void onScrollStateChanged(int state){

        if (state == RecyclerView.SCROLL_STATE_IDLE){
            LayoutManager layoutManager=getLayoutManager();
            int lastVisiblePosition;
            if (layoutManager instanceof GridLayoutManager){
                lastVisiblePosition=((GridLayoutManager) layoutManager).findLastVisibleItemPosition();

            }else if (layoutManager instanceof StaggeredGridLayoutManager){
                int into[]=new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager)layoutManager).findLastVisibleItemPositions(into);
                lastVisiblePosition=findMax(into);
            }else {
                lastVisiblePosition= ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
            if (layoutManager.getChildCount()>0 && lastVisiblePosition>=layoutManager.getItemCount()-1
                    &&layoutManager.getItemCount()>layoutManager.getChildCount()) {
                if (mListener!=null){
                    mListener.onLoad();
                }
            }

        }

    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }

        return max;
    }

    private void onLoad() {
        if (mListener != null) {
            mListener.onLoad();
        }
    }

    public interface OnLoadListener {
        void onLoad();
    }
}
