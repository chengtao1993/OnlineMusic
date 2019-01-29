package com.archermind.music.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.archermind.music.R;
import com.archermind.music.constants.Constants;
import com.archermind.music.http.HttpCallback;
import com.archermind.music.http.HttpClient;
import com.archermind.music.model.OnlineMusic;
import com.archermind.music.model.OnlineMusicList;
import com.archermind.music.model.SheetInfo;
import com.archermind.music.utils.FileUtils;
import com.bumptech.glide.Glide;

import java.util.List;

public class OnlineMusicAdapter extends BaseAdapter {

    public OnlineMusicAdapter(Context context, List<OnlineMusic> data,boolean mIslandscape) {
        super(context,data,mIslandscape);
        this.mData = data;
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setOnItemClickListener(View.OnClickListener onItemClickListener ){
        this.mOnItemClickListener = onItemClickListener;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = null ;
        if(viewType == Constants.ITEM_VIEW){
            if(islandscape){
                itemView = mInflater.inflate(R.layout.view_holder_online_music_list, viewGroup, false);
            } else{
                itemView = mInflater.inflate(R.layout.view_holder_online_music_list, viewGroup, false);
            }
            OnLineMusicViewHolder viewHolder = new OnLineMusicViewHolder(itemView);
            return viewHolder;
        }else{
            itemView = mInflater.inflate(R.layout.include_loading, viewGroup, false);
            FootViewHolder viewHolder = new FootViewHolder(itemView);

            //总数据小于一次获取的数据，不显示loading界面
            if(mData.size() < Constants.MUSIC_LIST_SIZE){
                viewHolder.setLoadState(Constants.LOAD_STATE_SUCCESS);
            }
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if(viewHolder instanceof  OnLineMusicViewHolder) {
            OnLineMusicViewHolder mViewHolder = ((OnLineMusicViewHolder) viewHolder);
            mViewHolder.setItemViewPosition(position);
            OnlineMusic onlineMusic = mData.get(position);
            Glide.with(mContext)
                    .load(onlineMusic.getPic_small())
                    .placeholder(R.drawable.default_cover)
                    .error(R.drawable.default_cover)
                    .into(mViewHolder.album_cover);
            mViewHolder.album_title.setText(onlineMusic.getTitle());
            String artist = FileUtils.getArtistAndAlbum(onlineMusic.getArtist_name(), onlineMusic.getAlbum_title());
            mViewHolder.album_artist.setText(artist);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return  super.getItemCount();

    }

    class OnLineMusicViewHolder extends RecyclerView.ViewHolder {
        private ImageView album_cover;
        private TextView  album_title;
        private TextView  album_artist;
        private View      mItemView;

        public OnLineMusicViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            if(mOnItemClickListener != null){
                mItemView.setOnClickListener(mOnItemClickListener);
            }

            album_cover =   (ImageView) mItemView.findViewById(R.id.push_album_cover);
            album_title = (TextView) mItemView.findViewById(R.id.push_title_1);
            album_artist = (TextView) mItemView.findViewById(R.id.push_title_2);
        }

        public void setItemViewPosition(int position){
            mItemView.setTag(position);
        }
    }

}
