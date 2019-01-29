package com.archermind.music.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.archermind.music.R;
import com.archermind.music.constants.Constants;
import com.archermind.music.model.OnlineMusic;
import com.archermind.music.widget.OnLoadListener;

import java.util.List;

public abstract  class BaseAdapter extends RecyclerView.Adapter <RecyclerView.ViewHolder> {
    public List<OnlineMusic> mData;
    public LayoutInflater mInflater;
    public Context mContext;
    public View.OnClickListener mOnItemClickListener;
    public boolean islandscape ;
    public boolean mIsLoadMore;
    public OnLoadListener mListener;

    public BaseAdapter(Context context, List<OnlineMusic> data, boolean mIslandscape) {
        this.mData = data;
        this.mContext = context;
        this.islandscape = mIslandscape;
        mInflater = LayoutInflater.from(context);
    }

    public void setOnItemClickListener(View.OnClickListener onItemClickListener ){
           this.mOnItemClickListener = onItemClickListener;
    }


    @Override
    public int getItemViewType(int position) {
        if (mData.isEmpty()){
            return Constants.FOOT_VIEW;
        }
        if ((position >= mData.size()) && mIsLoadMore){
            return Constants.FOOT_VIEW;
        } else{
            return Constants.ITEM_VIEW;
        }
    }


    @Override
    public int getItemCount() {

         if(mData.isEmpty()){
             return 1;
         }
         if((mData.size() > 0)&& mIsLoadMore){
             return mData.size()+1;
         }else{
             return mData.size();
         }

    }


    public class FootViewHolder extends RecyclerView.ViewHolder {
        private View     mItemView;
        private View     loadingView;
        private TextView loadingText;
        private TextView loadError;
        private TextView loadComplete;
        public FootViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mItemView = itemView;
            loadingView =    mItemView.findViewById(R.id.loading_view);
            loadingText =    mItemView.findViewById(R.id.tv_loading_start);
            loadError =      mItemView.findViewById(R.id.tv_loading_error);
            loadComplete =   mItemView.findViewById(R.id.tv_loading_complete);
        }

        public void setLoadState(int state){
            switch(state){
                case Constants.LOAD_STATE_LOADING:
                    loadingView.setVisibility(View.VISIBLE);
                    loadingText.setVisibility(View.VISIBLE);
                    loadError.setVisibility(View.GONE);
                    loadComplete.setVisibility(View.GONE);
                    break;
                case Constants.LOAD_STATE_FAIL:
                    loadingView.setVisibility(View.GONE);
                    loadingText.setVisibility(View.GONE);
                    loadError.setVisibility(View.VISIBLE);
                    loadComplete.setVisibility(View.GONE);
                    break;
                case Constants.LOAD_STATE_SUCCESS:
                    loadingView.setVisibility(View.GONE);
                    loadingText.setVisibility(View.GONE);
                    loadError.setVisibility(View.GONE);
                    loadComplete.setVisibility(View.VISIBLE);
                    break;
            }

        }
    }



    public void setLoadingListener(OnLoadListener listener){
        this.mListener  = listener ;

    }

    public void isLoadMore(boolean isLoadMore){
       this.mIsLoadMore = isLoadMore ;
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (isFooterView(holder.getLayoutPosition())) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) layoutManager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (isFooterView(position)) {
                        return gridManager.getSpanCount();
                    }
                    return 1;
                }
            });
        }
        initLoadMore(recyclerView);
    }


    private void initLoadMore(RecyclerView recyclerView) {
        if (!mIsLoadMore || mListener ==null) {
            return;
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    RecyclerView.LayoutManager layoutManager=recyclerView.getLayoutManager();
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
                    if (layoutManager.getChildCount()>0 && lastVisiblePosition>=layoutManager.getItemCount()-1 &&layoutManager.getItemCount()>layoutManager.getChildCount()) {
                        if (mListener !=null ){
                            mListener.loading();
                        }
                    }

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
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


    private boolean isFooterView(int position) {
        return (mIsLoadMore) && (position >= (getItemCount()-1));
    }

}
