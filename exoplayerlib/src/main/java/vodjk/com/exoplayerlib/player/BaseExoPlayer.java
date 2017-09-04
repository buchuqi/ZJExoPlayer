
package vodjk.com.exoplayerlib.player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import vodjk.com.exoplayerlib.builder.ExoPlayerMediaSourceBuilder;
import vodjk.com.exoplayerlib.listener.ControlToExoPlayer;
import vodjk.com.exoplayerlib.listener.ExoPlayerToControl;
import vodjk.com.exoplayerlib.listener.PlayerStateToView;
import vodjk.com.exoplayerlib.utils.HandlerWeak;
import vodjk.com.exoplayerlib.utils.ScreenSwitchUtils;
import vodjk.com.exoplayerlib.utils.VideoPlayUtils;
import vodjk.com.exoplayerlib.widget.ExoPlayerRootView;

public class BaseExoPlayer implements ControlToExoPlayer, ExoPlayer.EventListener {
    private static final String TAG = BaseExoPlayer.class.getName();
    //获取网速大小
    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;
    //进度
    private long resumePosition;
    private int resumeWindow;
    //播放器
    protected SimpleExoPlayer simpleExoPlayer;
    //View
    protected ExoPlayerRootView mPlayerView;
    //View层监听回调
    protected ExoPlayerToControl exoPlayerToControl;
    //视频播放状态回调
    protected PlayerStateToView playerStateToView;
    //加载多媒体载体
    protected ExoPlayerMediaSourceBuilder mediaSourceBuilder;
    private boolean playerNeedsSource;
    private NetworkBroadcastReceiver mNetworkBroadcastReceiver;
    protected List<String> videoUri;
    protected List<String> nameUri;
    protected Activity mContext;
    private boolean isPause;
    private HandlerWeak handler;
    //屏幕旋转监听
    private ScreenSwitchUtils screenSwitchUtils;

    /****
     * @param mContext 活动对象
     * @param uri      地址
     **/
    public BaseExoPlayer(@NonNull Activity mContext, ExoPlayerRootView playerView, @NonNull String uri) {
        this.mPlayerView = playerView;
        this.mContext = mContext;
        initView();
        setPlayUri(uri);
    }

    /****
     * @param mContext   活动对象
     * @param playerView 播放控件
     **/
    public BaseExoPlayer(@NonNull Activity mContext, @NonNull ExoPlayerRootView playerView) {
        this.mPlayerView = playerView;
        this.mContext = mContext;
        initView();
    }

    public BaseExoPlayer(@NonNull Activity mContext, @Nullable int reId) {
        this.mContext = mContext;
        mPlayerView = (ExoPlayerRootView) mContext.findViewById(reId);
        initView();
    }


    private void initView() {
        //防锁屏
        mContext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPlayerView.setExoPlayerListener(this);

        exoPlayerToControl = mPlayerView.getExoViewListener();

        //自适应旋转监听
        screenSwitchUtils = new ScreenSwitchUtils(mContext);

        handler = new HandlerWeak();
        handler.postDelayed(runnable, 1000);
    }


    /**
     * 定时刷新
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //间隔一秒刷新网络速度
            if (mPlayerView.getExoLoadingLayout().getVisibility() == View.VISIBLE) {
                exoPlayerToControl.showNetSpeed(getNetSpeed(), simpleExoPlayer == null ? "" : String.valueOf(simpleExoPlayer.getBufferedPercentage()));
            }
            if (simpleExoPlayer != null) {
                onPlayerCurrentPosition(simpleExoPlayer.getCurrentPosition());
            }
            handler.postDelayed(this, 1000);
        }
    };


    /**
     * 当前视频播放的位置
     *
     * @param current
     */
    protected void onPlayerCurrentPosition(long current) {
    }

    /**
     * 获取当前播放进度
     *
     * @return
     */
    public long getCurrentPosition() {
        simpleExoPlayer.stop();
        return simpleExoPlayer == null ? 0 : simpleExoPlayer.getCurrentPosition();
    }

    private int defalut;

    /**
     * 默认指定到播放位置
     *
     * @param defalut
     */
    public void seekToDefaultPosition(int defalut) {
        if (simpleExoPlayer == null) {
            return;
        }
        resumePosition = simpleExoPlayer.isCurrentWindowSeekable() ? Math.max(0, defalut * 1000)
                : C.TIME_UNSET;
    }

    /**
     * 设置播放路径
     ***/
    public void setPlayUri(@NonNull String uri) {
        setPlayUri(Uri.parse(uri));
    }

    /**
     * 播放带有广告的视频
     *
     * @param firstVideoUri
     * @param secondVideoUri
     */
    public void setADPlayUri(@NonNull String firstVideoUri, @NonNull String secondVideoUri) {
        if (mediaSourceBuilder != null) {
            mediaSourceBuilder.release();
        }
        this.mediaSourceBuilder = new ExoPlayerMediaSourceBuilder(mContext.getApplicationContext(), firstVideoUri, secondVideoUri);
        createPlayers();
        hslHideView();
        registerReceiverNet();
    }

    /***
     * 设置进度
     *
     * @param resumePosition 毫秒
     **/
    public void setPosition(long resumePosition) {
        this.resumePosition = resumePosition;
    }

    /***
     * 是否云隐藏
     **/
    void hslHideView() {
        //直播隐藏进度条
        if (mediaSourceBuilder != null && mediaSourceBuilder.getStreamType() == C.TYPE_HLS) {
            exoPlayerToControl.setProVisible(View.INVISIBLE);
        } else {
            exoPlayerToControl.setProVisible(View.VISIBLE);
        }
    }

    /**
     * 设置多线路播放
     *
     * @param videoUri 视频地址
     * @param name     清清晰度显示名称
     **/
    public void setPlaySwitchUri(@NonNull String[] videoUri, @NonNull String[] name) {
        setPlaySwitchUri(Arrays.asList(videoUri), Arrays.asList(name));
    }

    /**
     * 设置多线路播放
     *
     * @param videoUri 视频地址
     * @param name     清清晰度显示名称
     * @param index    选中播放线路
     **/
    public void setPlaySwitchUri(@NonNull String[] videoUri, @NonNull String[] name, int index) {
        setPlaySwitchUri(Arrays.asList(videoUri), Arrays.asList(name), index);
    }

    /**
     * 设置多线路播放
     *
     * @param videoUri 视频地址
     * @param name     清清晰度显示名称
     **/
    public void setPlaySwitchUri(@NonNull List<String> videoUri, @NonNull List<String> name) {
        setPlaySwitchUri(videoUri, name, 0);
    }

    /**
     * 设置多线路播放
     *
     * @param videoUri 视频地址
     * @param name     清清晰度显示名称
     * @param index    选中播放线路
     **/
    public void setPlaySwitchUri(@NonNull List<String> videoUri, @NonNull List<String> name, int index) {
        this.videoUri = videoUri;
        this.nameUri = name;
        exoPlayerToControl.showSwitchName(nameUri.get(index));
        this.mediaSourceBuilder = new ExoPlayerMediaSourceBuilder(mContext.getApplicationContext(), Uri.parse(videoUri.get(index)));
        createPlayers();
        hslHideView();
        registerReceiverNet();
    }

    private Uri uri;

    /**
     * 设置播放路径
     ***/
    public void setPlayUri(@NonNull Uri uri) {

        if (mediaSourceBuilder != null) {
            mediaSourceBuilder.release();
        }
        this.uri = uri;
        this.mediaSourceBuilder = new ExoPlayerMediaSourceBuilder(mContext.getApplicationContext(), uri);
        createPlayers();
        hslHideView();
        registerReceiverNet();
    }


    public void onResume() {
        if (simpleExoPlayer == null) {
//             if ((Util.SDK_INT <= 23 || simpleExoPlayer == null)) {
            createPlayers();
        }
        autoScreenOn();
    }


    public void onPause() {
        if (simpleExoPlayer != null) {
            isPause = !simpleExoPlayer.getPlayWhenReady();
            releasePlayers();
        }
        autoScreenOff();
    }

    private void autoScreenOn() {
        if (autoLandScreen) {
            screenSwitchUtils.start(mContext);
        }
    }

    private void autoScreenOff() {
        if (autoLandScreen) {
            screenSwitchUtils.stop();
        }
    }

    public void onDestroy() {
        releasePlayers();
    }

    public void releasePlayers() {
        if (simpleExoPlayer != null) {
            if (exoPlayerToControl != null) {
                exoPlayerToControl.releasePlayer();
            }
            updateResumePosition();
            simpleExoPlayer.stop();
            simpleExoPlayer.release();
            simpleExoPlayer.removeListener(this);
            simpleExoPlayer.clearVideoSurface();
            simpleExoPlayer = null;
            is_ReadyPlaye = false;
            isPlayerEnd = false;
            unNetworkBroadcastReceiver();
        }
        if (mContext.isFinishing()) {
            if (mediaSourceBuilder != null) {
                mediaSourceBuilder.release();
                mediaSourceBuilder = null;
            }
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
            //停止旋转屏幕
            screenSwitchUtils.stop();
            screenSwitchUtils.onDestoryScreenUtil();
            mPlayerView.setExoPlayerListener(null);
        }

    }


    /****
     * 创建播放器
     **/
    protected void createPlayers() {
        if (simpleExoPlayer == null) {
            simpleExoPlayer = createSimpleExoPlayer();
            playerNeedsSource = true;
        }
        startPlayVideo();
    }

    /****
     * 创建
     **/
    protected void createPlayersNo() {
        if (simpleExoPlayer == null) {
            simpleExoPlayer = createSimpleExoPlayer();
            playerNeedsSource = true;
        }
    }

    /****
     * 创建
     **/
    protected void createPlayersPlay() {
        simpleExoPlayer = createSimpleExoPlayer();
    }

    private SimpleExoPlayer createSimpleExoPlayer() {
        //从MediaSource 中选出 media 提供给可用的 Render S 来渲染
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
        //Create a default TrackSelector
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector);
        mPlayerView.setPlayer(player);
        return player;
    }

    /***
     * 开始播放视频
     **/
    public void startPlayVideo() {
        if (VideoPlayUtils.isWifi(mContext)) {
            onPlayVideo();
        } else {
            exoPlayerToControl.showAlertDialog();
        }
    }

    /***
     * 播放视频
     **/
    protected void onPlayVideo() {
        if (simpleExoPlayer == null) {
            createPlayersPlay();
        }
        if (mediaSourceBuilder == null) {
            return;
        }
        boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            simpleExoPlayer.seekTo(resumeWindow, resumePosition);
        }
        if (isPause) {
            simpleExoPlayer.setPlayWhenReady(false);
        } else {
            simpleExoPlayer.setPlayWhenReady(true);
        }
        simpleExoPlayer.prepare(mediaSourceBuilder.getMediaSource(), !haveResumePosition, true);
        simpleExoPlayer.addListener(this);
        playerNeedsSource = false;
        if (autoLandScreen) {
            //开始监听
            screenSwitchUtils.start(mContext);
        }
    }

    /****
     * 重置进度
     **/
    protected void updateResumePosition() {
        if (simpleExoPlayer != null) {
            resumeWindow = simpleExoPlayer.getCurrentWindowIndex();
            resumePosition = simpleExoPlayer.isCurrentWindowSeekable() ? Math.max(0, simpleExoPlayer.getCurrentPosition())
                    : C.TIME_UNSET;
        }
    }

    /**
     * 清除进度
     ***/
    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }


    /****
     * 获取当前网速
     *
     * @return String 二返回当前网速字符
     **/
    private String getNetSpeed() {
        String netSpeed;
        long nowTotalRxBytes = VideoPlayUtils.getTotalRxBytes(mContext);
        long nowTimeStamp = System.currentTimeMillis();
        long calculationTime = (nowTimeStamp - lastTimeStamp);
        if (calculationTime == 0) {
            netSpeed = String.valueOf(1) + " kb/s";
            return netSpeed;
        }
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / calculationTime);//毫秒转换
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        if (speed > 1024) {
            DecimalFormat df = new DecimalFormat("######0.0");
            netSpeed = String.valueOf(df.format(VideoPlayUtils.getM(speed))) + " MB/s";
        } else {
            netSpeed = String.valueOf(speed) + " kb/s";
        }
        return netSpeed;
    }


    /****
     * 监听返回键
     ***/
    public boolean onBackPressed() {
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (mPlayerView != null)
                mPlayerView.exitFullView();
            return true;
        } else {
            if (playerStateToView != null) {
                playerStateToView.onBack();
            }
            return false;
        }
    }

    /****
     * 滑动进度
     *
     * @param percent           滑动
     * @param seekTime          滑动的时间
     * @param seekTimePosition  滑动的时间 int
     * @param totalTime         视频总长
     * @param totalTimeDuration 视频总长 int
     **/
    protected void showProgressDialog(float percent, String seekTime, long seekTimePosition, String totalTime, long totalTimeDuration) {
    }


    protected void showBrightnessDialog(float percent) {
    }

    /****
     * 手势结束
     **/
    protected void endGesture() {
    }

    /****
     * 滑动音量
     *
     * @param percent 滑动
     **/
    protected void showVolumeDialog(float percent) {
    }


    /****
     * 滑动音量
     *
     * @param state 完成是否
     **/
    void showReplay(int state) {
    }

    public ExoPlayerRootView getPlayerView() {
        return mPlayerView;
    }

    private Configuration configuration;

    /****
     * 横竖屏切换
     *
     * @param configuration 旋转
     ***/
    public void onConfigurationChanged(Configuration configuration) {
        this.configuration = configuration;
        exoPlayerToControl.onConfigurationChanged(configuration.orientation);
    }

    /**
     * @return
     */
    public boolean isLandScreen() {
        if (configuration == null) {
            return false;
        }
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return true;
        }
        return false;

    }

    /***
     * 显示水印图
     *
     * @param res 资源
     ***/
    public void setExoPlayWatermarkImg(int res) {
        exoPlayerToControl.setWatermarkImage(res);
    }

    public void setTitle(String title) {
        exoPlayerToControl.setTitle(title);
    }

    /**
     * 视频
     *
     * @param playerStateToView
     */
    public void setVideoStateListener(PlayerStateToView playerStateToView) {
        this.playerStateToView = playerStateToView;

    }

    /**
     * 视频码率控制开关
     *
     * @param showVideoSwitch
     */
    public void isShowVideoSwitch(boolean showVideoSwitch) {
        mPlayerView.isSupportResoSwitch(showVideoSwitch);
    }

    public SimpleExoPlayer getSimpleExoPlayer() {
        return simpleExoPlayer;
    }

    /***
     * 注册广播监听
     **/
    protected void registerReceiverNet() {
        if (mNetworkBroadcastReceiver == null) {
            IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            mNetworkBroadcastReceiver = new NetworkBroadcastReceiver();
            mContext.registerReceiver(mNetworkBroadcastReceiver, intentFilter);
        }
    }

    /***
     * 取消广播监听
     **/
    private void unNetworkBroadcastReceiver() {
        if (mNetworkBroadcastReceiver != null) {
            mContext.unregisterReceiver(mNetworkBroadcastReceiver);
            mNetworkBroadcastReceiver = null;
        }
    }

    private class NetworkBroadcastReceiver extends BroadcastReceiver {
        long is = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isAvailable()) {
                    /////////////网络连接
                    if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        /////WiFi网络
                    } else if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        /////////3g网络
                        if (System.currentTimeMillis() - is > 500) {
                            is = System.currentTimeMillis();
                            updateResumePosition();
                            releasePlayers();
                            exoPlayerToControl.showAlertDialog();
                        }
                    }
                }
            }

        }
    }

    @Override
    public void onCreatePlayers() {
        createPlayers();
    }

    @Override
    public void onClearPosition() {
        clearResumePosition();
    }

    @Override
    public void replayPlayers() {
        clearResumePosition();
        onPlayVideo();
    }

    @Override
    public void switchUri(int position, String name) {
        if (mediaSourceBuilder != null) {
            mediaSourceBuilder.setMediaSourceUri(Uri.parse(videoUri.get(position)));
            updateResumePosition();
            onPlayVideo();
        }
    }

    @Override
    public void playVideoUri() {
        onPlayVideo();
    }

    @Override
    public BaseExoPlayer getPlay() {
        return BaseExoPlayer.this;
    }

    @Override
    public void onRePlayTrySee() {

    }

    @Override
    public void onshowTopControlVisible(int visible, boolean allwayshows) {
        if (playerStateToView == null) {
            return;
        }
        playerStateToView.onVisibilityChange(visible, allwayshows);

    }

    @Override
    public void isLockScreen(boolean isLockScreen) {
        if (isLockScreen) {
            autoScreenOff();
        } else {
            autoScreenOn();
        }
    }

    @Override
    public void showReplayViewChange(int visibility) {
        showReplay(visibility);
    }

    @Override
    public void onBack() {
        onBackPressed();
    }

    /***
     * 视频播放播放
     **/
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        Log.d(TAG, "onTimelineChanged:Timeline:getPeriodCount" + timeline.getPeriodCount());
        if (timeline.getPeriodCount() > 1) {
            if (simpleExoPlayer.getCurrentTrackGroups().length == 0) {
                exoPlayerToControl.setProVisible(View.INVISIBLE);
            } else {
                exoPlayerToControl.setProVisible(View.VISIBLE);
            }
        }
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        Log.d(TAG, "onTracksChanged:" + trackGroups.length);
    }

    /*****
     * 进度条控制 加载页
     *********/
    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.d(TAG, "onLoadingChanged:" + isLoading + "" + simpleExoPlayer.getPlayWhenReady());
    }

    private boolean isPlayerEnd = false;
    private boolean is_ReadyPlaye = false;

    /**
     * 视频的播放状态
     * STATE_IDLE 播放器空闲，既不在准备也不在播放
     * STATE_PREPARING 播放器正在准备
     * STATE_BUFFERING 播放器已经准备完毕，但无法立即播放。此状态的原因有很多，但常见的是播放器需要缓冲更多数据才能开始播放
     * STATE_PAUSE 播放器准备好并可以立即播放当前位置
     * STATE_PLAY 播放器正在播放中
     * STATE_ENDED 播放已完毕
     */
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.d(TAG, "onPlayerStateChanged:+playWhenReady:" + playWhenReady);
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                Log.d(TAG, "onPlayerStateChanged:加载中。。。");
                if (playWhenReady) {
                    exoPlayerToControl.setLoadStateVisible(View.VISIBLE);
                }
                if (playerStateToView != null) {
                    playerStateToView.onLoadingChanged();
                }
                break;
            case ExoPlayer.STATE_ENDED:
                isPlayerEnd = true;
                Log.d(TAG, "onPlayerStateChanged:ended。。。");
                exoPlayerToControl.setReplayVisible(View.VISIBLE);
                exoPlayerToControl.showTopControlVisible(View.VISIBLE);
                if (playerStateToView != null) {
                    playerStateToView.onPlayEnd();
                }
                break;
            case ExoPlayer.STATE_IDLE://空的
                if (isPlayerEnd || simpleExoPlayer == null) {
                    return;
                }
                if (mediaSourceBuilder == null || mediaSourceBuilder.getMediaSource() == null) {
                    return;
                }
                Log.d(TAG, "onPlayerStateChanged::网络状态差，请检查网络。。。" + simpleExoPlayer.getPlayWhenReady() + is_ReadyPlaye);
                updateResumePosition();
                if (!is_ReadyPlaye) {
                    return;
                }
                if (!VideoPlayUtils.isNetworkAvailable(mContext)) {
                    if (playerNeedsSource) {
                        exoPlayerToControl.setErrorStateVisible(View.VISIBLE);
                    }
                } else {
                    exoPlayerToControl.setErrorStateVisible(View.VISIBLE);
                }
                break;
            case ExoPlayer.STATE_READY:
                is_ReadyPlaye = true;
                isPlayerEnd = false;
                Log.d(TAG, "onPlayerStateChanged:ready。。。");
                exoPlayerToControl.setLoadStateVisible(View.GONE);
                if (playerStateToView != null) {
                    playerStateToView.onPlayStart();
                }
                if (autoLandScreen) {
                    //开始监听自己旋转屏幕
                    screenSwitchUtils.start(mContext);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        Log.e(TAG, "onPlayerError:" + e.getLocalizedMessage() + " 11111 " + e.getSourceException() + " 1111111 " + e.toString());
        if (e == null) {
            Log.e(TAG, "onPlayerError:" + e.getLocalizedMessage() + " 11111 " + e.getSourceException() + " 000000000000 " + e.toString());
            return;
        } else {
            Log.e(TAG, "onPlayerError:" + e.getLocalizedMessage() + " 11111 " + e.getSourceException() + " 22222 " + e.toString());
        }
        if (simpleExoPlayer == null) {
            return;
        }
        if (mediaSourceBuilder == null || mediaSourceBuilder.getMediaSource() == null) {
            return;
        }
        playerNeedsSource = true;
        if (VideoPlayUtils.isBehindLiveWindow(e)) {
            clearResumePosition();
            startPlayVideo();
        } else {
            exoPlayerToControl.setErrorStateVisible(View.VISIBLE);
            if (playerStateToView != null) {
                playerStateToView.onPlayerError(e);
            }
        }
    }

    @Override
    public void onPositionDiscontinuity() {
        Log.d(TAG, "onPositionDiscontinuity:");
        if (playerNeedsSource) {
            updateResumePosition();
        }
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    }

    //自动横竖屏
    private boolean autoLandScreen = true;

    /**
     * 自动横竖屏
     *
     * @param autoLandScreen
     */
    public void autoLandScreen(boolean autoLandScreen) {
        this.autoLandScreen = autoLandScreen;
    }


    public boolean isPlaying() {
        return simpleExoPlayer.isCurrentWindowSeekable();
    }
}

