# ZJExoPlayer
![](https://github.com/buchuqi/ZJExoPlayer/blob/master/images/device-2017-09-04-172315.png)
![](https://github.com/buchuqi/ZJExoPlayer/blob/master/images/device-2017-09-04-171905.png)
![](https://github.com/buchuqi/ZJExoPlayer/blob/master/images/device-2017-09-04-175315.png)
![](https://github.com/buchuqi/ZJExoPlayer/blob/master/images/device-2017-09-04-175705.png)

功能：

* 支持锁屏
* 支持试看功能
* 支持StatusBar状态类
* 支持自适应横竖屏切换
* 支持声音、亮度、进度手势操作
* 支持广告播放，然后播放视频内容
* 支持多种播放格式支持HTTP直播了（HLS),MP4,MP3,WebM,M4A,MPEG-TS 和 AAC(基于exoplayer) 




# Xml里配置
```

<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <vodjk.com.exoplayerlib.widget.ExoPlayerView
        android:id="@+id/exo_play_context_id"
        android:layout_width="match_parent"
        android:layout_height="@dimen/y405"  <!--控件的高度-->
        android:background="@android:color/transparent"
        exo:controller_layout_id="@layout/simple_exo_playback_control_view"
        exo:exoplayer_height="@dimen/y405"   <!--视频默认高度-->
        exo:fastforward_increment="0"
        exo:paddingEnd="0dp"
        exo:paddingStart="0dp"
        exo:player_layout_id="@layout/simple_exo_view"
        exo:resize_mode="fit"
        exo:rewind_increment="0"
        exo:seekbar_allwayshow="true"         <!--进度条一直显示,false的话如果是直播流不显示-->
        exo:show_timeout="3000"               <!--点击显示时长-->
        exo:support_lock_screen="true"        <!--是否支持锁屏-->
        exo:support_switch_resolution="false" <!--是否支持清晰度切换-->
        exo:surface_type="surface_view"
        exo:topbar_transparent="true"         <!--顶部状态栏是否顶满-->
        exo:use_artwork="true"                <!--是否使用加载默认图-->
        exo:use_controller="true" />

    <LinearLayout
        android:id="@+id/content"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        ......

    </LinearLayout>


</LinearLayout>

 
```

Activity里的配置
```
  @Override
    protected void onCreate(Bundle savedInstanceState) {
    exoPlayerView = (ExoPlayerView) findViewById(R.id.exo_play_context_id);
    exoPlayerView.initPlayer(videoType == null ? ExoPlayerType.Standard : videoType)
                .setPlayUri(videoInfo.url)
                .loadArtWork(videoInfo.cover)
                .isAutoLandScreen(true)//是否自动应旋转屏幕
                .isLockScreen(true)    //是否支持锁屏
                .seekToPosition(videoInfo.current) //默认播放起始位置
                ..setonPlayerStateListener(new PlayerStateToView() {

                    @Override
                    public void onPlayStart() {

                    }

                    @Override
                    public void onLoadingChanged() {

                    }

                    @Override
                    public void onPlayerError(ExoPlaybackException e) {

                    }

                    @Override
                    public void onPlayEnd() {

                    }

                    @Override
                    public void onBack() {

                    }

                    @Override
                    public void onVisibilityChange(int visible, boolean isAllWayShowTop) {

                    }

                    @Override
                    public void onBuyVideo() {

                    }
                });
                
                }
                
                
    @Override
    public void onResume() {
        super.onResume();
        exoPlayerView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayerView == null) {
            return;
        }
        exoPlayerView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayerView.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //横竖屏切换，content为视频布局之外的布局为了全屏使用
        exoPlayerView.onConfigurationChanged(newConfig, content);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        //使用播放返回键监听
        exoPlayerView.onBackPressed();
         // TODO: 处理自己的业务逻辑
         ...
         
        finish();
        super.onBackPressed();
    }

```
