package com.archermind.music.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.archermind.music.R;
import com.archermind.music.adapter.OnMoreClickListener;
import com.archermind.music.adapter.OnlineMusicAdapter;
import com.archermind.music.adapter.PlaylistAdapter;
import com.archermind.music.fragment.PlayFragment;
import com.archermind.music.model.Music;
import com.archermind.music.service.AudioPlayer;
import com.archermind.music.service.OnPlayerEventListener;
import com.archermind.music.utils.binding.Bind;
import com.archermind.music.widget.OnRecyclerViewItemClickListener;

/**
 * 播放列表
 */
public class PlaylistActivity extends BaseActivity implements  View.OnClickListener,OnPlayerEventListener {
    private OnlineMusicAdapter mAdapter;
    private ImageButton backBtn ;
    private RecyclerView recyclerView;
    private PlayFragment mPlayFragment;
    private boolean isPlayFragmentShow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        recyclerView = findViewById(R.id.play_music_listview);
        GridLayoutManager mGridLayoutMgr = new GridLayoutManager(this,2);
        mGridLayoutMgr.setOrientation(GridLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(mGridLayoutMgr);
        backBtn = findViewById(R.id.play_music_back);
        backBtn.setOnClickListener(this);
    }

    @Override
    protected void onServiceBound() {
//        mAdapter = new OnlineMusicAdapter(this, mMusicList);
//        mAdapter.setOnItemClickListener(onRecyclerViewItemClickListener);
//        recyclerView.setAdapter(mAdapter);
//        AudioPlayer.get().addOnPlayEventListener(this);
    }


    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AudioPlayer.get().play(position);
    }



    @Override
    public void onChange(Music music) {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayerStart() {
    }

    @Override
    public void onPlayerPause() {
    }

    @Override
    public void onPublish(int progress) {
    }

    @Override
    public void onBufferingUpdate(int percent) {
    }

    @Override
    protected void onDestroy() {
        AudioPlayer.get().removeOnPlayEventListener(this);
        super.onDestroy();
    }


    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onClick(View v) {
            super.onClick(v);
            int position = (int) v.getTag();
            AudioPlayer.get().play(position);
           // showPlayingFragment();
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_music_back:
                this.finish();
                overridePendingTransition(R.anim.fragment_slide_up, R.anim.fragment_slide_down);

        }
    }
}
