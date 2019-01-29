package com.archermind.music.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.archermind.music.R;
import com.archermind.music.adapter.BaseAdapter;
import com.archermind.music.adapter.PushMusicListAdapter;
import com.archermind.music.constants.Constants;
import com.archermind.music.constants.Extras;
import com.archermind.music.executor.PlayOnlineMusic;
import com.archermind.music.fragment.PlayFragment;
import com.archermind.music.http.HttpCallback;
import com.archermind.music.http.HttpClient;
import com.archermind.music.model.Music;
import com.archermind.music.model.OnlineMusic;
import com.archermind.music.model.OnlineMusicList;
import com.archermind.music.model.SheetInfo;
import com.archermind.music.service.AudioPlayer;
import com.archermind.music.utils.ImageUtils;
import com.archermind.music.utils.ToastUtils;
import com.archermind.music.widget.OnLoadListener;
import com.archermind.music.widget.OnRecyclerViewItemClickListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;
import java.util.List;


public class PushMusicListActivity extends BaseActivity implements View.OnClickListener,OnLoadListener {

    private RecyclerView recyclerView;
    private TextView title;
    private SheetInfo mListInfo;
    private ImageButton backBtn;
    private View vHeader;
    private OnlineMusicList mOnlineMusicList;
    private List<OnlineMusic> mMusicList = new ArrayList<>();
    private int mOffset = 0;
    private PushMusicListAdapter mAdapter;
    private boolean isPlayFragmentShow;
    private PlayFragment mPlayFragment;
    private  LinearLayoutManager mLinearLayoutMgr;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_music);
        boolean isLandscape = isOrientationLandscape();
        recyclerView = findViewById(R.id.push_listview);
        mLinearLayoutMgr = new LinearLayoutManager(this);
        if(isLandscape){
            mLinearLayoutMgr.setOrientation(LinearLayoutManager.HORIZONTAL);
        }else{
            mLinearLayoutMgr.setOrientation(LinearLayoutManager.VERTICAL);
        }
        recyclerView.setLayoutManager(mLinearLayoutMgr);
        backBtn = findViewById(R.id.push_music_back);
        backBtn.setOnClickListener(this);


    }

    @Override
    protected void onServiceBound() {
        mListInfo = (SheetInfo) getIntent().getSerializableExtra(Extras.MUSIC_LIST_TYPE);
        getMusic(mOffset);
        mAdapter = new PushMusicListAdapter(this, mMusicList,isOrientationLandscape());
        mAdapter.setOnItemClickListener(onRecyclerViewItemClickListener);
        mAdapter.setLoadingListener(this);
        mAdapter.isLoadMore(true);
        recyclerView.setAdapter(mAdapter);
    }

    private void getMusic(final int offset) {
        HttpClient.getSongListInfo(mListInfo.getType(), Constants.MUSIC_LIST_SIZE, offset, new HttpCallback<OnlineMusicList>() {
            @Override
            public void onSuccess(OnlineMusicList response) {
                onloadSuccess();
                mOnlineMusicList = response;
                if (offset == 0 && response == null) {
                    return;
                } else if (offset == 0) {
                    initHeader();
                }
                if (response == null || response.getSong_list() == null || response.getSong_list().size() == 0) {
                    return;
                }
                mOffset += Constants.MUSIC_LIST_SIZE;
                mMusicList.addAll(response.getSong_list());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(Exception e) {
                onloadFail();
                if (e instanceof RuntimeException) {
                    return;
                }
                if (offset == 0) {

                } else {
                  ToastUtils.show(R.string.load_fail);
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.push_music_back:
                this.finish();
                overridePendingTransition(R.anim.fragment_slide_up, R.anim.fragment_slide_down);

        }
    }


    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onClick(View v) {
            super.onClick(v);
            int position = (int) v.getTag();
            play(mMusicList.get(position));
            showPlayingFragment();
        }
    };

    private void initHeader() {
        final ImageView ivHeaderBg = findViewById(R.id.iv_header_bg);
        final ImageView ivCover = findViewById(R.id.iv_cover);
        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvUpdateDate = findViewById(R.id.tv_update_date);
        TextView tvComment = findViewById(R.id.tv_comment);
        tvTitle.setText(mOnlineMusicList.getBillboard().getName());
        tvUpdateDate.setText(getString(R.string.recent_update, mOnlineMusicList.getBillboard().getUpdate_date()));
        tvComment.setText(mOnlineMusicList.getBillboard().getComment());
        Glide.with(this)
                .load(mOnlineMusicList.getBillboard().getPic_s640())
                .asBitmap()
                .placeholder(R.drawable.default_cover)
                .error(R.drawable.default_cover)
                .override(200, 200)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        ivCover.setImageBitmap(resource);
                        ivHeaderBg.setImageBitmap(ImageUtils.blur(resource));
                    }
                });
    }

    private void play(OnlineMusic onlineMusic) {
        new PlayOnlineMusic(this, onlineMusic) {
            @Override
            public void onPrepare() {
                showProgress();
            }

            @Override
            public void onExecuteSuccess(Music music) {
                cancelProgress();
                AudioPlayer.get().addAndPlay(music);
                ToastUtils.show("已添加到播放列表");
            }

            @Override
            public void onExecuteFail(Exception e) {
                cancelProgress();
                ToastUtils.show(R.string.unable_to_play);
            }
        }.execute();
    }
    private void showPlayingFragment() {
        if (isPlayFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (mPlayFragment == null) {
            mPlayFragment = new PlayFragment();
            ft.replace(android.R.id.content, mPlayFragment);
        } else {
            ft.show(mPlayFragment);
        }
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = true;
    }

    private void hidePlayingFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(mPlayFragment);
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = false;
    }

    @Override
    public void onBackPressed() {
        if (mPlayFragment != null && isPlayFragmentShow) {
            hidePlayingFragment();
            return;
        }
        super.onBackPressed();
    }


    public void setLoadState(int state){
        View loadingView = recyclerView.getChildAt(recyclerView.getChildCount()-1);
        if(loadingView != null){
            BaseAdapter.FootViewHolder holder = (BaseAdapter.FootViewHolder)recyclerView.getChildViewHolder(loadingView);
            holder.setLoadState(state);
        }else{
        }
    }

    @Override
    public void loading() {
        getMusic(mOffset);
        setLoadState(Constants.LOAD_STATE_LOADING);

    }

    @Override
    public void onloadFail() {
        setLoadState(Constants.LOAD_STATE_FAIL);

    }

    @Override
    public void onloadSuccess() {
       setLoadState(Constants.LOAD_STATE_SUCCESS);
    }
}