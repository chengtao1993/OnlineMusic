package com.archermind.music.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.archermind.music.R;
import com.archermind.music.adapter.OnMoreClickListener;
import com.archermind.music.adapter.SearchMusicAdapter;
import com.archermind.music.enums.LoadStateEnum;
import com.archermind.music.executor.DownloadSearchedMusic;
import com.archermind.music.executor.PlaySearchedMusic;
import com.archermind.music.executor.ShareOnlineMusic;
import com.archermind.music.http.HttpCallback;
import com.archermind.music.http.HttpClient;
import com.archermind.music.model.Music;
import com.archermind.music.model.SearchMusic;
import com.archermind.music.service.AudioPlayer;
import com.archermind.music.tagview.TagContainerLayout;
import com.archermind.music.tagview.TagView;
import com.archermind.music.utils.FileUtils;
import com.archermind.music.utils.ToastUtils;
import com.archermind.music.utils.ViewUtils;
import com.archermind.music.utils.binding.Bind;

public class SearchMusicActivity extends BaseActivity implements SearchView.OnQueryTextListener
        , AdapterView.OnItemClickListener, OnMoreClickListener,View.OnClickListener,TagView.OnTagClickListener,TextWatcher {
    @Bind(R.id.lv_search_music_list)
    private ListView lvSearchMusic;
    @Bind(R.id.ll_loading)
    private LinearLayout llLoading;
    @Bind(R.id.ll_load_fail)
    private LinearLayout llLoadFail;
    private ImageButton searchBackbtn ;
    private ImageButton searchSubmitbtn ;
    private ImageButton searchClearbtn ;
    private EditText searchInput ;
    private List<SearchMusic.Song> searchMusicList = new ArrayList<>();
    private SearchMusicAdapter mAdapter = new SearchMusicAdapter(searchMusicList);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_music);
        ArrayList<String> hotList = new ArrayList<String>();
        hotList.add("沙漠骆驼");
        hotList.add("一百万种可能");
        hotList.add("火箭少女");
        hotList.add("暗里着迷");
        hotList.add("过把瘾");
        hotList.add("SNK48");


        ArrayList<String> historyList = new ArrayList<String>();
        historyList.add("BEYOND");
        historyList.add("刘德华");
        historyList.add("燃烧我的卡路里");
        historyList.add("纸醉金迷");
        historyList.add("流浪");
        historyList.add("千万次的问");
        historyList.add("陈慧琳");
        historyList.add("光辉岁月");
        historyList.add("让我一次爱个够");

        TagContainerLayout hotTagLayout = (TagContainerLayout) findViewById(R.id.search_hot_tag_layout);
        hotTagLayout.setTagTextDirection(View.TEXT_DIRECTION_LTR);
        hotTagLayout.setTags(hotList);
        hotTagLayout.setOnTagClickListener(this);

        TagContainerLayout historyTagLayout = (TagContainerLayout) findViewById(R.id.search_history_tag_layout);
        historyTagLayout.setTagTextDirection(View.TEXT_DIRECTION_LTR);
        historyTagLayout.setTags(historyList);
        historyTagLayout.setOnTagClickListener(this);


    }


    @Override
    protected void onServiceBound() {
        lvSearchMusic.setAdapter(mAdapter);
        TextView tvLoadFail = llLoadFail.findViewById(R.id.tv_load_fail_text);
        tvLoadFail.setText(R.string.search_empty);
        searchBackbtn = findViewById(R.id.search_music_back);
        searchSubmitbtn = findViewById(R.id.search_submit_btn);
        searchClearbtn = findViewById(R.id.search_clear_btn);
        searchInput = findViewById(R.id.search_music_input);
        searchBackbtn.setOnClickListener(this);
        searchSubmitbtn.setOnClickListener(this);
        lvSearchMusic.setOnItemClickListener(this);
        searchClearbtn.setOnClickListener(this);
        mAdapter.setOnMoreClickListener(this);
        searchInput.addTextChangedListener(this);
    }

    @Override
    protected int getDarkTheme() {
        return R.style.AppThemeDark_Search;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_music, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.onActionViewExpanded();
        searchView.setQueryHint(getString(R.string.search_tips));
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);
        try {
            Field field = searchView.getClass().getDeclaredField("mGoButton");
            field.setAccessible(true);
            ImageView mGoButton = (ImageView) field.get(searchView);
            mGoButton.setImageResource(R.drawable.ic_menu_search);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOADING);
        searchMusic(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void searchMusic(String keyword) {
        HttpClient.searchMusic(keyword, new HttpCallback<SearchMusic>() {
            @Override
            public void onSuccess(SearchMusic response) {
                if (response == null || response.getSong() == null) {
                    ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                    return;
                }
                ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                searchMusicList.clear();
                searchMusicList.addAll(response.getSong());
                mAdapter.notifyDataSetChanged();
                lvSearchMusic.requestFocus();
                handler.post(() -> lvSearchMusic.setSelection(0));
            }

            @Override
            public void onFail(Exception e) {
                ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        new PlaySearchedMusic(this, searchMusicList.get(position)) {
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

    @Override
    public void onMoreClick(int position) {
        final SearchMusic.Song song = searchMusicList.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(song.getSongname());
        String path = FileUtils.getMusicDir() + FileUtils.getMp3FileName(song.getArtistname(), song.getSongname());
        File file = new File(path);
        int itemsId = file.exists() ? R.array.search_music_dialog_no_download : R.array.search_music_dialog;
        dialog.setItems(itemsId, (dialog1, which) -> {
            switch (which) {
                case 0:// 分享
                    share(song);
                    break;
                case 1:// 下载
                    download(song);
                    break;
            }
        });
        dialog.show();
    }

    private void share(SearchMusic.Song song) {
        new ShareOnlineMusic(this, song.getSongname(), song.getSongid()) {
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

    private void download(final SearchMusic.Song song) {
        new DownloadSearchedMusic(this, song) {
            @Override
            public void onPrepare() {
                showProgress();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                cancelProgress();
                ToastUtils.show(getString(R.string.now_download, song.getSongname()));
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
            case R.id.search_music_back:
                this.finish();
                overridePendingTransition(R.anim.fragment_slide_up, R.anim.fragment_slide_down);
                break;
            case R.id.search_submit_btn:
                String query = searchInput.getText().toString().trim();
                if(null != query && !query.equals("")){
                    ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOADING);
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                    searchMusic(query);
                }
            case R.id.search_clear_btn:
                searchInput.setText("");
                break;
        }
    }



    @Override
    public void onTagClick(int position, String text) {
       searchInput.setText(text);
       searchInput.setSelection(text.length());
    }

    @Override
    public void onTagLongClick(int position, String text) {

    }

    @Override
    public void onTagCrossClick(int position) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
    String word = editable.toString();
    if(word.length() > 0 && (searchClearbtn.getVisibility() == View.GONE)){
        searchClearbtn.setVisibility(View.VISIBLE);
    }
    if(word.length() == 0 && (searchClearbtn.getVisibility() == View.VISIBLE)){
        searchClearbtn.setVisibility(View.GONE);
        }

    }
}
