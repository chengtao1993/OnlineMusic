package com.archermind.music.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import com.archermind.music.R;
import com.archermind.music.adapter.PushMusicAdapter;
import com.archermind.music.application.AppCache;
import com.archermind.music.constants.Extras;
import com.archermind.music.model.SheetInfo;
import com.archermind.music.widget.OnRecyclerViewItemClickListener;

import java.util.List;

public class HomeActivity extends BaseActivity implements View.OnClickListener {
    private static final int MUSIC_LIST_SIZE = 20;
    private RecyclerView recyclerView;
    private List<SheetInfo> mSongLists;
    private ObjectAnimator discObjectAnimator;
    private CardView onlineMusicBtn;
    private CardView collectionMusicBtn;
    private ImageButton playListBtn ;
    private TextView title;
    private  LinearLayoutManager mLinearLayoutMgr;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isLandscape = isOrientationLandscape();
        initViewAndData(isLandscape);
    }




    public void initViewAndData(boolean isLandscape){
        mLinearLayoutMgr = new LinearLayoutManager(this);
        if(isLandscape){
            setContentView(R.layout.activity_home_land);
            mLinearLayoutMgr.setOrientation(LinearLayoutManager.HORIZONTAL);
        }else{
            setContentView(R.layout.activity_home_port);
            mLinearLayoutMgr.setOrientation(LinearLayoutManager.VERTICAL);
        }
        recyclerView = findViewById(R.id.push_listview);
        onlineMusicBtn = findViewById(R.id.online_music_layout);
        playListBtn = findViewById(R.id.home_search_music);
        title = findViewById(R.id.home_music_title);
        playListBtn.setOnClickListener(this);
        onlineMusicBtn.setOnClickListener(this);
        collectionMusicBtn = findViewById(R.id.collection_music_layout);
        collectionMusicBtn.setOnClickListener(this);
        recyclerView.setLayoutManager(mLinearLayoutMgr);
        getSheetInfo();
        togglePlayAnimation(true);
    }

    private void getSheetInfo() {
        mSongLists = AppCache.get().getSheetList();
        if (mSongLists.isEmpty()) {
            String[] titles = getResources().getStringArray(R.array.online_music_list_title);
            String[] types = getResources().getStringArray(R.array.online_music_list_type);
            for (int i = 0; i < titles.length; i++) {
                if (!types[i].equals("#")) {
                    SheetInfo info = new SheetInfo();
                    info.setTitle(titles[i]);
                    info.setType(types[i]);
                    mSongLists.add(info);
                }
            }
        }
        PushMusicAdapter adapter = new PushMusicAdapter(this, mSongLists,isOrientationLandscape());
        adapter.setOnItemClickListener(onRecyclerViewItemClickListener);
        recyclerView.setAdapter(adapter);

    }

    private void togglePlayAnimation(boolean isPlay) {
        ImageButton discView = findViewById(R.id.home_play_status);
        discObjectAnimator = ObjectAnimator.ofFloat(discView, "rotation", 0, 360);
        discObjectAnimator.setDuration(15000);
        //使ObjectAnimator动画匀速平滑旋转
        discObjectAnimator.setInterpolator(new LinearInterpolator());
        //无限循环旋转
        discObjectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        discObjectAnimator.setRepeatMode(ValueAnimator.RESTART);
        if (isPlay) {
            discObjectAnimator.start();
        } else {
            discObjectAnimator.cancel();
        }

    }
    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener(){
        @Override
        public void onClick(View v) {
            super.onClick(v);
            int position = (int)v.getTag();
            SheetInfo sheetInfo = mSongLists.get(position);
            Intent intent = new Intent(HomeActivity.this, PushMusicListActivity.class);
            intent.putExtra(Extras.MUSIC_LIST_TYPE, sheetInfo);
            startActivity(intent);
            overridePendingTransition(R.anim.fragment_slide_up, R.anim.fragment_slide_down);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.online_music_layout :
            SheetInfo sheetInfo = mSongLists.get(0);
            Intent intent = new Intent(this, OnlineMusicActivity.class);
            intent.putExtra(Extras.MUSIC_LIST_TYPE, sheetInfo);
            startActivity(intent);
            overridePendingTransition(R.anim.fragment_slide_up, R.anim.fragment_slide_down);
            findViewById(R.id.online_music_wave).setVisibility(View.VISIBLE);
            findViewById(R.id.collection_music_wave).setVisibility(View.INVISIBLE);
            title.setText(getString(R.string.online_music_txt));
            break;
        case R.id.collection_music_layout :
            findViewById(R.id.online_music_wave).setVisibility(View.INVISIBLE);
            findViewById(R.id.collection_music_wave).setVisibility(View.VISIBLE);
            title.setText(getString(R.string.personal_collection_txt));
            break;
        case R.id.home_search_music:
            Intent cIntent = new Intent(this, SearchMusicActivity.class);
            startActivity(cIntent);
            overridePendingTransition(R.anim.fragment_slide_up, R.anim.fragment_slide_down);
            break;
        }
    }


    @Override

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean orientation = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE?true:false;
        initViewAndData(orientation);
      }
    }
