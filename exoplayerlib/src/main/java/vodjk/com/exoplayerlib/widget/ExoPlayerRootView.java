package vodjk.com.exoplayerlib.widget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import vodjk.com.exoplayerlib.R;
import vodjk.com.exoplayerlib.listener.ControlToExoPlayer;
import vodjk.com.exoplayerlib.listener.ExoPlayerToControl;
import vodjk.com.exoplayerlib.utils.StatusBar;
import vodjk.com.exoplayerlib.utils.VideoPlayUtils;

import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;


/**
 * 视频播放器类
 */
public class ExoPlayerRootView extends FrameLayout implements PlaybackControlView.VisibilityListener, View.OnClickListener, ExoPlayerToControl {
    public static final String TAG = "VideoPlayerView";
    protected Activity mContext;
    ///播放view
    protected SimpleExoPlayerView simpleExoPlayerView;
    //全屏或者竖屏
    private ImageButton exo_video_fullscreen;
    //默认顶部导航
    private RelativeLayout exo_top_control;
    //视视频标题
    protected TextView exo_controls_title;
    //自定义control视频标题
    private TextView top_controls_title;
    //清晰度切换
    protected TextView exo_video_switch;
    //实时视频加载速度显示
    protected TextView exo_loading_show_text;
    //视频加载页
    private View exo_loading_layout;
    //错误页
    private View exo_play_error_layout;
    //进度控件
    private DefaultTimeBar timeBar;
    //播放结束
    private View exo_play_replay_layout;
    //提示布局
    private View exo_play_btn_hint_layout;
    //水印
    private ImageView exoPlayWatermark;
    //切换清晰度
    private ResoSwitcherPop resoSwitcherPop;
    //topcontrol
    private LinearLayout top_back_control;
    //锁屏布局
    private FrameLayout exo_lock_screen_frame;
    //锁屏Checkbox
    private CheckBox lock_iv_switch;
    //是否锁屏
    private boolean isLockScreen;
    //视频布局高度
    protected float videoHeight;
    //水印ID
    int rourceWatermarkID = 0;
    private AlertDialog alertDialog;
    private Lock lock = new ReentrantLock();
    /**
     * 是否支持清晰度切换
     */
    private boolean isSupportResoSwitch = false;
    private boolean isSupportUseArtwork = true;

    protected ControlToExoPlayer mControlToExoPlayer;
    //是否支持锁屏
    private boolean isSupportLock = true;

    public ExoPlayerRootView(Context context) {
        this(context, null);
    }

    public ExoPlayerRootView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoPlayerRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addExoView(attrs);
        intiView(context);
        afterView();
    }

    private void addExoView(AttributeSet attrs) {
        initAttts(getContext(), attrs);
        simpleExoPlayerView = new SimpleExoPlayerView(getContext(), attrs);
        simpleExoPlayerView.setResizeMode(RESIZE_MODE_FILL);
        simpleExoPlayerView.setControllerVisibilityListener(this);
        addView(simpleExoPlayerView);

    }

    private boolean supportTopbarTrans = false;
    //进度条一直显示
    private boolean seekbarllwayShow = false;

    /**
     * * 设置水印
     * xml app:user_watermark="@mipmap/watermark_big"
     *
     * @param context
     * @param attrs
     */
    public void initAttts(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ExoPlayerRootView, 0, 0);
            try {
                rourceWatermarkID = a.getResourceId(R.styleable.ExoPlayerRootView_user_watermark, 0);
                isSupportLock = a.getBoolean(R.styleable.ExoPlayerRootView_support_lock_screen, true);
                isSupportUseArtwork = a.getBoolean(R.styleable.ExoPlayerRootView_default_artwork, true);
                isSupportResoSwitch = a.getBoolean(R.styleable.ExoPlayerRootView_support_switch_resolution, false);
                setVideoHeight(a.getDimension(R.styleable.ExoPlayerRootView_exoplayer_height, 405));
                supportTopbarTrans = a.getBoolean(R.styleable.ExoPlayerRootView_topbar_transparent, false);
                seekbarllwayShow = a.getBoolean(R.styleable.ExoPlayerRootView_seekbar_allwayshow, false);
            } finally {
                a.recycle();
            }
        }
    }


    public boolean isSeekbarllwayShow() {
        return seekbarllwayShow;
    }


    private void afterView() {
        setWaterMark(rourceWatermarkID);
        isSupporLockScreen(isSupportLock);
        isSupportResoSwitch(isSupportResoSwitch);
        isSupportUseArtwork(isSupportUseArtwork);
    }

    /**
     * 是否支持锁屏功能
     *
     * @param isSupporLock
     */
    public void isSupporLockScreen(boolean isSupporLock) {
        this.isSupportLock = isSupporLock;
    }

    /**
     * 锁屏Touch监听
     */
    private OnTouchListener lockTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    handler.removeCallbacks(hideLockRun);
                    break;
                case MotionEvent.ACTION_UP:
                    if (lock_iv_switch.getVisibility() == VISIBLE) {
                        setLockIvSwitchVisi(INVISIBLE);
                    } else {
                        setLockIvSwitchVisi(VISIBLE);
                        handler.postDelayed(hideLockRun, 3000);
                    }
                    break;
            }
            return isLockScreen;
        }
    };


    private Runnable hideLockRun = new Runnable() {
        @Override
        public void run() {
            if (lock_iv_switch.getVisibility() == VISIBLE) {
                setLockIvSwitchVisi(INVISIBLE);
            } else {
                handler.removeCallbacks(hideLockRun);
            }
        }
    };
    /**
     * 是否锁屏
     */
    private CompoundButton.OnCheckedChangeListener lockCheckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            isLockScreen = b;
            if (isLockScreen) {
                exo_lock_screen_frame.setVisibility(VISIBLE);
            } else {
                exo_lock_screen_frame.setVisibility(GONE);
            }
            if (mControlToExoPlayer != null) {
                mControlToExoPlayer.isLockScreen(b);
            }

        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void onVisibilityChange(int visibility) {
        if (resoSwitcherPop != null && visibility == View.GONE) {
            resoSwitcherPop.dismissBelowView();
        }

        if (isLandScreen(newConfig) && isSupportLock)
            setLockIvSwitchVisi(visibility);

        if (mControlToExoPlayer != null) {
            mControlToExoPlayer.onshowTopControlVisible(visibility, isShowTopControl);
        }

        if (isShowTopControl) {
            return;
        }
        top_back_control.setVisibility(visibility);
    }

    private int newConfig = Configuration.ORIENTATION_PORTRAIT;

    /***
     * 判断是横屏,竖屏
     *
     * @param newConfig 旋转对象
     */
    private void doOnConfigurationChanged(int newConfig) {
        this.newConfig = newConfig;
        switch (newConfig) {
            case Configuration.ORIENTATION_LANDSCAPE:
                if (mContext instanceof AppCompatActivity) {
                    AppCompatActivity activity = (AppCompatActivity) mContext;
                    if (activity.getSupportActionBar() != null) {
                        activity.getSupportActionBar().hide();
                    }
                }
                this.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                //获得 WindowManager.LayoutParams 属性对象
                WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
                //直接对它flags变量操作   LayoutParams.FLAG_FULLSCREEN 表示设置全屏
                lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                //设置属性
                mContext.getWindow().setAttributes(lp);
                //允许窗口扩展到屏幕之外
                //mContext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                //skin的宽高
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                if (mContext instanceof AppCompatActivity) {
                    AppCompatActivity activity = (AppCompatActivity) mContext;
                    if (activity.getSupportActionBar() != null) {
                        activity.getSupportActionBar().show();
                    }
                }
                simpleExoPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                //获得 WindowManager.LayoutParams 属性对象
                WindowManager.LayoutParams lp2 = mContext.getWindow().getAttributes();
                //LayoutParams.FLAG_FULLSCREEN 强制屏幕状态条栏弹出
                lp2.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                //设置属性
                mContext.getWindow().setAttributes(lp2);
                if (supportTopbarTrans) {//顶部状态栏是否透明顶到边
                    StatusBar.setStatusBarTranslucent(mContext, false);
                } else {//正常显示
                    mContext.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    //显示状态栏
                    mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
                break;
        }
        scaleLayout(newConfig);
        showSwitch(newConfig);
        showLockScreen(newConfig);
    }


    private void showLockScreen(int newConfig) {
        if (!isSupportLock || exo_play_replay_layout.getVisibility() == VISIBLE) return;
        if (isLandScreen(newConfig)) {
            setLockIvSwitchVisi(VISIBLE);
            exo_video_switch.setOnClickListener(this);
        } else {
            if (exo_lock_screen_frame.getVisibility() == VISIBLE) {
                lock_iv_switch.setChecked(false);
            }
            setLockIvSwitchVisi(INVISIBLE);
        }
    }

    private void setLockIvSwitchVisi(int visible) {
        lock_iv_switch.setVisibility(visible);
    }

    /**
     * 设置videoFrame的大小
     *
     * @param newConfig
     */
    protected void scaleLayout(int newConfig) {
        ViewGroup.LayoutParams params = getLayoutParams();
        if (isLandScreen(newConfig)) {
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(outMetrics);
            params.height = outMetrics.heightPixels;
        } else {
            params.height = (int) videoHeight;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        setLayoutParams(params);
    }

    /***
     * 是否显示切换清晰按钮
     *
     * @param newConfig 是否横竖屏
     **/
    private void showSwitch(int newConfig) {
        if (!isSupportResoSwitch) return;
        if (isLandScreen(newConfig)) {
            exo_video_switch.setVisibility(View.VISIBLE);
            exo_video_switch.setOnClickListener(this);
        } else {
            exo_video_switch.setVisibility(View.GONE);
        }
    }

    /**
     * 是否是横屏
     *
     * @param newConfig
     * @return
     */
    private boolean isLandScreen(int newConfig) {
        if (newConfig == Configuration.ORIENTATION_LANDSCAPE) {
            return true;
        } else {
            return false;
        }
    }

    /***
     * 显示隐藏加载页
     *
     * @param state 状态
     ***/
    private void showLoadState(int state) {
        Log.d(TAG, "showLoadState:" + state);
        if (exo_loading_layout != null) {
            exo_loading_layout.setVisibility(state);
        }
        if (state == View.VISIBLE) {
            setErrorStateVisi(View.GONE);
            showReplay(View.GONE);
        }
        //一直显示顶部tab
        isShowTopControl = false;
    }

    /***
     * 显示隐藏错误页
     *
     * @param state 状态
     ***/
    private void setErrorStateVisi(int state) {
        if (state == View.VISIBLE) {
            showLoadState(View.GONE);
            showReplay(View.GONE);
            simpleExoPlayerView.setOnTouchListener(null);
        }
        if (exo_play_error_layout != null) {
            exo_play_error_layout.setVisibility(state);
        }
    }

    /**
     * 设置视频默认图片
     *
     * @param defaultArtwork
     */
    public void setArtwork(Bitmap defaultArtwork) {
        simpleExoPlayerView.setDefaultArtwork(defaultArtwork);
    }

    /**
     * 是否支持视频默认图
     *
     * @param useArtwork
     */
    public void isSupportUseArtwork(boolean useArtwork) {
        simpleExoPlayerView.setUseArtwork(useArtwork);
    }

    /***
     * 显示隐藏重播页
     *
     * @param state 状态
     ***/
    private void showReplay(int state) {
        if (exo_play_replay_layout != null) {
            exo_play_replay_layout.setVisibility(state);
        }
        if (state == View.VISIBLE) {
            showLoadState(View.GONE);
            setErrorStateVisi(View.GONE);
        }
    }

    /***
     * 显示按钮提示页
     *
     * @param state 状态
     ***/
    protected void showBtnContinueHint(int state) {
        if (state == View.VISIBLE) {
            showLoadState(View.GONE);
            showReplay(View.GONE);
            setErrorStateVisi(View.GONE);
        }
        if (exo_play_btn_hint_layout != null) {
            exo_play_btn_hint_layout.setVisibility(state);
        }
    }

    /****
     * 监听返回键
     ***/
    public void exitFullView() {
        mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        exo_video_fullscreen.setImageResource(R.drawable.ic_fullscreen_white);
        doOnConfigurationChanged(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    public View getExoLoadingLayout() {
        return exo_loading_layout;
    }

    public void setExoPlayerListener(ControlToExoPlayer mControlToExoPlayer) {
        this.mControlToExoPlayer = mControlToExoPlayer;
    }

    /**
     * 是否支持分辨率切换
     *
     * @param isSupport
     */
    public void isSupportResoSwitch(boolean isSupport) {
        isSupportResoSwitch = isSupport;
        if (isSupportResoSwitch) {
            exo_video_switch.setVisibility(View.VISIBLE);
        } else {
            exo_video_switch.setVisibility(View.GONE);
        }
    }

    /**
     * 关联布局播多媒体类
     *
     * @param player 多媒体类
     ***/
    public void setPlayer(SimpleExoPlayer player) {
        simpleExoPlayerView.setPlayer(player);
    }


    public SimpleExoPlayerView getSimpleExoPlayerView() {
        return simpleExoPlayerView;
    }

    public void setVideoHeight(float videoHeight) {
        this.videoHeight = videoHeight;
    }

    public ExoPlayerToControl getExoViewListener() {
        return this;
    }

    /**
     * 设置水印
     *
     * @param waterMark
     */
    public void setWaterMark(int waterMark) {
        if (waterMark != 0) {
            exoPlayWatermark.setImageResource(waterMark);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.exo_video_fullscreen) {
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {//横屏
                mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                exo_video_fullscreen.setImageResource(R.mipmap.ic_fullscreen_white);
                doOnConfigurationChanged(Configuration.ORIENTATION_PORTRAIT);
            } else if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {//竖屏
                mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                exo_video_fullscreen.setImageResource(R.mipmap.ic_fullscreen_exit_white);
                doOnConfigurationChanged(Configuration.ORIENTATION_LANDSCAPE);
            }
        } else if (v.getId() == R.id.exo_controls_back) {
            if (mControlToExoPlayer != null)
                mControlToExoPlayer.onBack();
        } else if (v.getId() == R.id.exo_play_error_btn) {
            if (mControlToExoPlayer == null) {
                return;
            }
            if (VideoPlayUtils.isNetworkAvailable(mContext)) {
                setErrorStateVisible(View.GONE);
                if (mControlToExoPlayer != null)
                    mControlToExoPlayer.onCreatePlayers();
            } else {
                Toast.makeText(mContext, R.string.net_network_no_hint, Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.exo_video_replay) {
            if (VideoPlayUtils.isNetworkAvailable(mContext)) {
                setReplayVisible(View.GONE);
                showLockScreen(newConfig);
                if (mControlToExoPlayer != null)
                    mControlToExoPlayer.replayPlayers();
            } else {
                Toast.makeText(mContext, R.string.net_network_no_hint, Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.exo_video_switch) {//切换
            if (resoSwitcherPop == null) {
                resoSwitcherPop = new ResoSwitcherPop(mContext);
                resoSwitcherPop.setOnItemClickListener(new ResoSwitcherPop.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position, String name) {
                        resoSwitcherPop.dismissBelowView();
                        exo_video_switch.setText(name);
                        if (mControlToExoPlayer != null)
                            mControlToExoPlayer.switchUri(position, name);
                    }
                });
            }
            if (resoSwitcherPop != null)
                resoSwitcherPop.showBelowView(v, true);
        } else if (v.getId() == R.id.top_controls_back) {
            if (isLandScreen(newConfig)) {
                exitFullView();
            } else {
                if (mControlToExoPlayer != null)
                    mControlToExoPlayer.onBack();
            }
        } else if (v.getId() == R.id.exo_play_btn_hint) {//提示播放
            showDialog();
        }
    }

    @Override
    public void showAlertDialog() {
        showDialog();
    }

    @Override
    public void setProVisible(int visibility) {
        if (seekbarllwayShow) {
            timeBar.setVisibility(VISIBLE);
        } else {
            timeBar.setVisibility(visibility);
        }
    }

    @Override
    public void setWatermarkImage(int res) {
        setWaterMark(res);//视频水印
    }

    @Override
    public void showSwitchName(String name) {
        exo_video_switch.setText(name);
    }

    @Override
    public void setLoadStateVisible(int visibility) {
        showLoadState(visibility);
        isSupporLockScreen(isSupportLock);
    }

    @Override
    public void setReplayVisible(int visibility) {
        showReplay(visibility);
        if (visibility == VISIBLE) {
            hideLockScreenView();
        }
    }

    @Override
    public void setErrorStateVisible(int visibility) {
        setErrorStateVisi(visibility);
        if (visibility == VISIBLE) {
            hideLockScreenView();
        }
    }

    @Override
    public void setTitle(String title) {
        exo_controls_title.setText(title);
        top_controls_title.setText(title);
    }

    /***
     * 显示网速
     *
     * @param netSpeed 网速的值
     ***/
    @Override
    public void showNetSpeed(final String netSpeed, final String bufferPercentage) {
        handler.obtainMessage(Index_NetSpeed, netSpeed).sendToTarget();
    }

    @Override
    public void onConfigurationChanged(int newConfig) {
        doOnConfigurationChanged(newConfig);
    }

    //自定义顶部导航一直显示
    private boolean isShowTopControl;

    @Override
    public void showTopControlVisible(int visibility) {
        top_back_control.setVisibility(visibility);
        if (visibility == VISIBLE) {
            isShowTopControl = true;
        }
    }

    private final int Index_RELEASEPLAYER = 1;
    private final int Index_NetSpeed = 2;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Index_RELEASEPLAYER:
                    showLoadState(GONE);
                    setReplayVisible(GONE);
                    setErrorStateVisible(GONE);
                    break;
                case Index_NetSpeed:
                    if (exo_loading_show_text != null) {
                        exo_loading_show_text.setText((String) msg.obj);
                    }
                    break;
            }

        }
    };

    @Override
    public void releasePlayer() {
        handler.sendEmptyMessage(Index_RELEASEPLAYER);
    }


    public void onDestroy() {
        if (alertDialog != null) {
            alertDialog = null;
        }
        if (lock != null) {
            lock = null;
        }
        if (resoSwitcherPop != null) {
            resoSwitcherPop = null;
        }
    }

    /**
     * 隐藏锁屏按钮
     */
    private void hideLockScreenView() {
        if (lock_iv_switch.getVisibility() == VISIBLE) {
            setLockIvSwitchVisi(INVISIBLE);
        }
    }

    /***
     * 显示网络提示框
     ***/
    private void showDialog() {
        try {
            lock.lock();
            if (alertDialog != null && alertDialog.isShowing()) {
                return;
            }
            alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setTitle(mContext.getString(R.string.exo_play_reminder));
            alertDialog.setMessage(mContext.getString(R.string.exo_play_wifi_hint_no));
            alertDialog.setCancelable(false);
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    showBtnContinueHint(View.VISIBLE);
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    showBtnContinueHint(View.GONE);
                    mControlToExoPlayer.playVideoUri();
                }
            });
            alertDialog.show();
        } finally {
            lock.unlock();
        }
    }

    private void intiView(Context context) {
        mContext = (Activity) context;
        //提示
        exo_play_btn_hint_layout = simpleExoPlayerView.findViewById(R.id.exo_play_btn_hint_layout);
        simpleExoPlayerView.findViewById(R.id.exo_play_btn_hint).setOnClickListener(this);
        //错误状态
        exo_play_error_layout = simpleExoPlayerView.findViewById(R.id.exo_play_error_layout);
        simpleExoPlayerView.findViewById(R.id.exo_play_error_btn).setOnClickListener(this);
        //重新播放
        exo_play_replay_layout = simpleExoPlayerView.findViewById(R.id.exo_play_replay_layout);
        simpleExoPlayerView.findViewById(R.id.exo_video_replay).setOnClickListener(this);
        //加载中状态
        exo_loading_layout = simpleExoPlayerView.findViewById(R.id.exo_loading_layout);
        exo_loading_show_text = (TextView) simpleExoPlayerView.findViewById(R.id.exo_loading_show_text);
        //底部状态控制
        timeBar = (DefaultTimeBar) simpleExoPlayerView.findViewById(R.id.exo_progress);
        exo_video_switch = (TextView) simpleExoPlayerView.findViewById(R.id.exo_video_switch);
        exo_video_fullscreen = (ImageButton) simpleExoPlayerView.findViewById(R.id.exo_video_fullscreen);
        exo_video_fullscreen.setOnClickListener(this);
        //自定义顶部状态
        top_back_control = (LinearLayout) simpleExoPlayerView.findViewById(R.id.top_back_control);
        simpleExoPlayerView.findViewById(R.id.top_controls_back).setOnClickListener(this);
        top_controls_title = (TextView) simpleExoPlayerView.findViewById(R.id.top_controls_title);
        //exoplayer 顶部状态
        exo_top_control = (RelativeLayout) simpleExoPlayerView.findViewById(R.id.exo_top_control);
        exo_controls_title = (TextView) simpleExoPlayerView.findViewById(R.id.exo_controls_title);
        simpleExoPlayerView.findViewById(R.id.exo_controls_back).setOnClickListener(this);
        //水印状态
        exoPlayWatermark = (ImageView) simpleExoPlayerView.findViewById(R.id.exo_play_watermark);
        //锁屏状态
        exo_lock_screen_frame = (FrameLayout) simpleExoPlayerView.findViewById(R.id.exo_lock_screen_frame);
        exo_lock_screen_frame.setOnTouchListener(lockTouchListener);
        lock_iv_switch = (CheckBox) simpleExoPlayerView.findViewById(R.id.lock_iv_switch);
        lock_iv_switch.setOnCheckedChangeListener(lockCheckListener);
    }

}
