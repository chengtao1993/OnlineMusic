package com.archermind.music.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.archermind.music.adapter.BaseAdapter;
import com.archermind.music.adapter.PushMusicListAdapter;
import com.archermind.music.constants.Constants;
import com.archermind.music.fragment.PlayFragment;
import com.archermind.music.widget.OnLoadListener;
import com.archermind.music.widget.OnRecyclerViewItemClickListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.archermind.music.R;
import com.archermind.music.adapter.OnMoreClickListener;
import com.archermind.music.adapter.OnlineMusicAdapter;
import com.archermind.music.constants.Extras;
import com.archermind.music.enums.LoadStateEnum;
import com.archermind.music.executor.DownloadOnlineMusic;
import com.archermind.music.executor.PlayOnlineMusic;
import com.archermind.music.executor.ShareOnlineMusic;
import com.archermind.music.http.HttpCallback;
import com.archermind.music.http.HttpClient;
import com.archermind.music.model.Music;
import com.archermind.music.model.OnlineMusic;
import com.archermind.music.model.OnlineMusicList;
import com.archermind.music.model.SheetInfo;
import com.archermind.music.service.AudioPlayer;
import com.archermind.music.utils.FileUtils;
import com.archermind.music.utils.ImageUtils;
import com.archermind.music.utils.ScreenUtils;
import com.archermind.music.utils.ToastUtils;
import com.archermind.music.utils.ViewUtils;
import com.archermind.music.utils.binding.Bind;
import com.archermind.music.widget.AutoLoadListView;

public class OnlineMusicActivity extends BaseActivity implements View.OnClickListener,OnLoadListener {

    private SheetInfo mListInfo;
    private OnlineMusicList mOnlineMusicList;
    private List<OnlineMusic> mMusicList = new ArrayList<>();
    private OnlineMusicAdapter mAdapter;
    private int mOffset = 0;
    private ImageButton backBtn ;
    private RecyclerView recyclerView;
    private PlayFragment mPlayFragment;
    private boolean isPlayFragmentShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_music);
        recyclerView = findViewById(R.id.online_listview);
        GridLayoutManager mGridLayoutMgr = new GridLayoutManager(this,2);
        mGridLayoutMgr.setOrientation(GridLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(mGridLayoutMgr);
        backBtn = findViewById(R.id.online_music_back);
        backBtn.setOnClickListener(this);
    }

    @Override
    protected void onServiceBound() {
        mListInfo = (SheetInfo) getIntent().getSerializableExtra(Extras.MUSIC_LIST_TYPE);
        // title.setText(mListInfo.getTitle());
        getMusic(mOffset);
        mAdapter = new OnlineMusicAdapter(this, mMusicList,isOrientationLandscape());
        mAdapter.setOnItemClickListener(onRecyclerViewItemClickListener);
        mAdapter.setLoadingListener(this);
        mAdapter.isLoadMore(true);
        recyclerView.setAdapter(mAdapter);
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

    private void getMusic(final int offset) {
        HttpClient.getSongListInfo(mListInfo.getType(), Constants.MUSIC_LIST_SIZE, offset, new HttpCallback<OnlineMusicList>() {
            @Override
            public void onSuccess(OnlineMusicList response) {
                onloadSuccess();
                mOnlineMusicList = response;
                if (offset == 0 && response == null) {
                    return;
                } else if (offset == 0) {
                    //initHeader();
                }
                if (response == null || response.getSong_list() == null || response.getSong_list().size() == 0) {
                    return;
                }
                mOffset += Constants.MUSIC_LIST_SIZE;
                mMusicList.addAll(response.getSong_list());
                for(OnlineMusic info:mMusicList ){
                    Log.e("hct","{music = "+ info+"/n}");
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(Exception e) {
                if (e instanceof RuntimeException) {
                    onloadFail();
                    return;
                }
                if (offset == 0) {
                } else {
                    ToastUtils.show(R.string.load_fail);
                }
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

    private void share(final OnlineMusic onlineMusic) {
        new ShareOnlineMusic(this, onlineMusic.getTitle(), onlineMusic.getSong_id()) {
            @Override
            public void onPrepare() {
                showProgress();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                cancelProgress();
            }

            @Override
            public void onExecuteFail(Exception e) {
                cancelProgress();
            }
        }.execute();
    }

    private void artistInfo(OnlineMusic onlineMusic) {
        ArtistInfoActivity.start(this, onlineMusic.getTing_uid());
    }

    private void download(final OnlineMusic onlineMusic) {
        new DownloadOnlineMusic(this, onlineMusic) {
            @Override
            public void onPrepare() {
                showProgress();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                cancelProgress();
                ToastUtils.show(getString(R.string.now_download, onlineMusic.getTitle()));
            }

            @Override
            public void onExecuteFail(Exception e) {
                cancelProgress();
                ToastUtils.show(R.string.unable_to_download);
            }
        }.execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.online_music_back:
                this.finish();
                overridePendingTransition(R.anim.fragment_slide_up, R.anim.fragment_slide_down);

        }
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
