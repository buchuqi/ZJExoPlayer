package vodjk.com.exoplayerlib.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import vodjk.com.exoplayerlib.R;
import vodjk.com.exoplayerlib.listener.ExoOnDialogClickListener;
import vodjk.com.exoplayerlib.listener.ExoPlayerType;
import vodjk.com.exoplayerlib.listener.PlayerStateToView;
import vodjk.com.exoplayerlib.player.ADExoPlayer;
import vodjk.com.exoplayerlib.player.BaseExoPlayer;
import vodjk.com.exoplayerlib.player.OnlyBuyExoPlayer;
import vodjk.com.exoplayerlib.player.PayExoPlayer;
import vodjk.com.exoplayerlib.player.StandardExoPlayer;

/**
 * 视频播放
 * Created by jian_zhou on 2017/8/18.
 */

public class ExoPlayerView extends ExoPlayerRootView {

    public ExoPlayerView(Context context) {
        this(context, null);
    }

    public ExoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPlayer(ExoPlayerType.Standard);
    }

    private ExoPlayerType seclectTpye = null;
    private BaseExoPlayer exoPlayer = null;

    public ExoPlayerView initPlayer(ExoPlayerType type) {
        if (seclectTpye == null || seclectTpye != type) {
            if (exoPlayer != null) {
                exoPlayer.releasePlayers();
                exoPlayer = null;
            }
        }
        if (exoPlayer == null) {
            exoPlayer = getExoPlayerByType(type);
        }
        seclectTpye = type;
        return this;
    }

    public ExoPlayerView setPlayUri(String uri) {
        if (exoPlayer == null) {
            initPlayer(seclectTpye == null ? ExoPlayerType.Standard : seclectTpye);
        }
        exoPlayer.setPlayUri(uri);
        return this;
    }


    public ExoPlayerView loadArtWork(String url) {
        Glide.with(mContext).load(url).asBitmap().fitCenter().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onLoadStarted(Drawable placeholder) {
                setArtwork(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.bg_default_video));
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
            }

            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                setArtwork(resource);
            }
        });
        return this;
    }

    private PlayerStateToView listener = null;

    /**
     * 设置监听状态
     *
     * @param listener
     * @return
     */
    public ExoPlayerView setonPlayerStateListener(PlayerStateToView listener) {
        exoPlayer.setVideoStateListener(listener);
        return this;
    }

    /**
     * 设置试看秒数
     *
     * @param second
     * @return
     */
    public ExoPlayerView setFreeSeeSecond(int second) {
        if (exoPlayer != null && exoPlayer instanceof PayExoPlayer) {
            ((PayExoPlayer) exoPlayer).setFreeSeeSecond(second);
        }
        return this;
    }

    /**
     * 设置是否自适应旋转屏幕
     *
     * @param autoLandScreen
     * @return
     */
    public ExoPlayerView isAutoLandScreen(boolean autoLandScreen) {
        exoPlayer.autoLandScreen(autoLandScreen);
        return this;
    }


    /**
     * 设置是否开启锁屏功能
     *
     * @param lockScreen
     * @return
     */
    public ExoPlayerView isLockScreen(boolean lockScreen) {
        isSupporLockScreen(lockScreen);
        return this;
    }


    public boolean isPlaying() {
        return exoPlayer.isPlaying();
    }

    /**
     * 设置是否允许切换分辨率
     *
     * @param autoLandScreen
     * @return
     */
    public ExoPlayerView isResoSwitch(boolean autoLandScreen) {
        exoPlayer.isShowVideoSwitch(autoLandScreen);
        return this;
    }


    /**
     * 获取播放器
     *
     * @param type
     * @return
     */
    private BaseExoPlayer getExoPlayerByType(ExoPlayerType type) {
        BaseExoPlayer baseExoPlayer = null;
        if (type == ExoPlayerType.Pay) {
            baseExoPlayer = new PayExoPlayer(mContext, this);
        } else if (type == ExoPlayerType.Standard) {
            baseExoPlayer = new StandardExoPlayer(mContext, this);
        } else if (type == ExoPlayerType.AD) {
            baseExoPlayer = new ADExoPlayer(mContext, this);
        } else if (type == ExoPlayerType.OnlyBuy) {
            baseExoPlayer = new OnlyBuyExoPlayer(mContext, this);
        }
        return baseExoPlayer;
    }

    public void onResume() {
        if (exoPlayer == null) {
            return;
        }
        exoPlayer.onResume();
    }

    public void onPause() {
        if (exoPlayer == null) {
            return;
        }
        exoPlayer.onPause();
    }

    public void onDestroy() {
        if (exoPlayer == null) {
            return;
        }
        exoPlayer.onDestroy();
    }

    /**
     * 横屏处理
     *
     * @param newConfig
     */
    public void onConfigurationChanged(Configuration newConfig) {
        exoPlayer.onConfigurationChanged(newConfig);//横竖屏切换
    }

    /**
     * @param newConfig
     * @param contentView 视频之外的分布局为了做全屏处理
     */
    public void onConfigurationChanged(Configuration newConfig, View contentView) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            contentView.setVisibility(View.GONE);
        } else {
            contentView.setVisibility(View.VISIBLE);
        }
        exoPlayer.onConfigurationChanged(newConfig);//横竖屏切换
    }

    public void onBackPressed() {
        if (exoPlayer == null) {
            return;
        }
        exoPlayer.onBackPressed();//使用播放返回键监听
    }

    public void seekToPosition(long position) {
        exoPlayer.setPosition(position * 1000);
    }

    /**
     * 释放播放器
     */
    public void releasePlayers() {
        if (exoPlayer == null) {
            return;
        }
        exoPlayer.releasePlayers();
        exoPlayer = null;
    }


    /**
     * 获取当前播放进度
     *
     * @return
     */
    public long getCurrentPosition() {
        return exoPlayer == null ? 0 : exoPlayer.getCurrentPosition();
    }


    /**
     * 指定到默认播放位置
     *
     * @param defaul
     */
    public void seekToDefaultPosition(int defaul) {
        exoPlayer.seekToDefaultPosition(defaul);
    }


    /**
     * exoPlayerManager.setADPlayUri("http://120.25.246.21/vrMobile/travelVideo/zhejiang_xuanchuanpian.mp4",
     * "http://v.cctv.com/flash/mp4video6/TMS/2011/01/05/cf752b1c12ce452b3040cab2f90bc265_h264818000nero_aac32-1.mp4");
     *
     * @param adUrl
     * @param url
     * @return
     */
    public ExoPlayerView setADPlayUri(String adUrl, String url) {
        exoPlayer.setADPlayUri(adUrl, url);
        return this;
    }

    /**
     * String[] test = {"http://120.25.246.21/vrMobile/travelVideo/zhejiang_xuanchuanpian.mp4",
     * "http://120.25.246.21/vrMobile/travelVideo/zhejiang_xuanchuanpian.mp4",
     * "http://120.25.246.21/vrMobile/travelVideo/zhejiang_xuanchuanpian.mp4"};
     * String[] name = {"超清", "高清", "标清"};
     *
     * @param urls
     * @param names
     * @return
     */
    public ExoPlayerView setSwitchPlayUri(String[] urls, String[] names) {
        exoPlayer.setPlaySwitchUri(urls, names);
        return this;
    }


    public void setOnlyBuyVisible(int visible) {
        if (exoPlayer instanceof OnlyBuyExoPlayer) {
            ((OnlyBuyExoPlayer) exoPlayer).setOnlyBuyRootVisi(visible);
        }
    }

    /**
     * 设置弹出框
     *
     * @param islooper
     * @param title
     * @param message
     * @param left
     * @param right
     * @param isHiddenCancleBtn
     * @param listener
     */
    public void setDialogConfig(boolean islooper, String title, String message, String left, String right, final boolean isHiddenCancleBtn, final ExoOnDialogClickListener listener) {
        if (exoPlayer instanceof StandardExoPlayer) {
            ((StandardExoPlayer) exoPlayer).setDialogConfig(islooper, title, message, left, right, isHiddenCancleBtn, listener);
        }
    }

}
