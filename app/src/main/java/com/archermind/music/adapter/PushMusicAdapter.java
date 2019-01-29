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
import com.archermind.music.http.HttpCallback;
import com.archermind.music.http.HttpClient;
import com.archermind.music.model.OnlineMusic;
import com.archermind.music.model.OnlineMusicList;
import com.archermind.music.model.SheetInfo;
import com.bumptech.glide.Glide;

import java.util.List;

public class PushMusicAdapter extends RecyclerView.Adapter <PushMusicAdapter.PushMusicViewHolder> {
    private List<SheetInfo>  mData;
    private LayoutInflater mInflater;
    private Context mContext;
    private View.OnClickListener mOnItemClickListener;
    private boolean islandscape ;

    public PushMusicAdapter(Context context, List<SheetInfo>  data,boolean mIslandscape) {
        this.mData = data;
        this.mContext = context;
        this.islandscape = mIslandscape;
        mInflater = LayoutInflater.from(context);
    }

    public void setOnItemClickListener(View.OnClickListener onItemClickListener ){
           this.mOnItemClickListener = onItemClickListener;
    }


    @NonNull
    @Override
    public PushMusicAdapter.PushMusicViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView  =null;
        if(islandscape){
            itemView = mInflater.inflate(R.layout.view_holder_push_music_land, viewGroup, false);
        }else{
            itemView = mInflater.inflate(R.layout.view_holder_push_music_port, viewGroup, false);
        }

        PushMusicViewHolder viewHolder = new PushMusicViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PushMusicAdapter.PushMusicViewHolder viewHolder, int position) {
        // 设置数据
        SheetInfo sheetInfo = mData.get(position);
        viewHolder.setItemViewPosition(position);
        getMusicListInfo(sheetInfo, viewHolder);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    private void getMusicListInfo(final SheetInfo sheetInfo, final PushMusicViewHolder viewHolder) {
        if (sheetInfo.getCoverUrl() == null) {
            viewHolder.album_title_1.setTag(sheetInfo.getTitle());
            viewHolder.album_cover.setImageResource(R.drawable.default_cover);
            viewHolder.album_title_1.setText("1.加载中…");
            viewHolder.album_title_2.setText("2.加载中…");
            viewHolder.album_title_3.setText("3.加载中…");
            HttpClient.getSongListInfo(sheetInfo.getType(), 3, 0, new HttpCallback<OnlineMusicList>() {
                @Override
                public void onSuccess(OnlineMusicList response) {

                    if (response == null || response.getSong_list() == null) {
                        Log.e("auto_music","null");
                        return;
                    }
                    if (!sheetInfo.getTitle().equals(viewHolder.album_title_1.getTag())) {
                        return;
                    }
                    parse(response, sheetInfo);
                    setData(sheetInfo, viewHolder);
                }

                @Override
                public void onFail(Exception e) {
                    Log.e("auto_music","ERROR");
                }
            });
        } else {
            //viewHolder.album_cover.setTag(null);
            setData(sheetInfo, viewHolder);
        }
    }

    private void parse(OnlineMusicList response, SheetInfo sheetInfo) {
        List<OnlineMusic> onlineMusics = response.getSong_list();
        sheetInfo.setCoverUrl(response.getBillboard().getPic_s260());
        if (onlineMusics.size() >= 1) {
            sheetInfo.setMusic1(mContext.getString(R.string.song_list_item_title_1, onlineMusics.get(0).getTitle(), onlineMusics.get(0).getArtist_name()));
        } else {
            sheetInfo.setMusic1("");
        }
        if (onlineMusics.size() >= 2) {
            sheetInfo.setMusic2(mContext.getString(R.string.song_list_item_title_2, onlineMusics.get(1).getTitle(), onlineMusics.get(1).getArtist_name()));
        } else {
            sheetInfo.setMusic2("");
        }
        if (onlineMusics.size() >= 3) {
            sheetInfo.setMusic3(mContext.getString(R.string.song_list_item_title_3, onlineMusics.get(2).getTitle(), onlineMusics.get(2).getArtist_name()));
        } else {
            sheetInfo.setMusic3("");
        }
    }

    private void setData(SheetInfo sheetInfo, final PushMusicViewHolder viewHolder) {
        viewHolder.album_title_1.setText(sheetInfo.getMusic1());
        viewHolder.album_title_2.setText(sheetInfo.getMusic2());
        viewHolder.album_title_3.setText(sheetInfo.getMusic3());
        Glide.with(mContext)
                .load(sheetInfo.getCoverUrl())
                .placeholder(R.drawable.default_cover)
                .error(R.drawable.default_cover)
                .into(viewHolder.album_cover);
    }


    class PushMusicViewHolder extends RecyclerView.ViewHolder {
       private ImageView album_cover;
       private TextView  album_title_1;
       private TextView  album_title_2;
       private TextView  album_title_3;
       private View      mItemView;

        public PushMusicViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            if(mOnItemClickListener != null){
                mItemView.setOnClickListener(mOnItemClickListener);
            }

            album_cover =   (ImageView) mItemView.findViewById(R.id.push_album_cover);
            album_title_1 = (TextView) mItemView.findViewById(R.id.push_title_1);
            album_title_2 = (TextView) mItemView.findViewById(R.id.push_title_2);
            album_title_3 = (TextView) mItemView.findViewById(R.id.push_title_3);
        }

        public void setItemViewPosition(int position){
            mItemView.setTag(position);
        }
    }

}
