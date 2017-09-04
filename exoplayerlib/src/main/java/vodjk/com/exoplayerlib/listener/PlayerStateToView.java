package vodjk.com.exoplayerlib.listener;

import com.google.android.exoplayer2.ExoPlaybackException;

/**
 * 视频播放状态
 */
public interface PlayerStateToView {
    /***
     * 开始播放
     * **/
    void onPlayStart();

    /***
     * 播放是否加载中
     * **/
    void onLoadingChanged();

    /***
     * 播放失败
     * @param e  异常
     * **/
    void onPlayerError(ExoPlaybackException e);

    /***
     * 播放结束
     * **/
    void onPlayEnd();

    /**
     * 返回
     */
    void onBack();

    /**
     * 显示状态
     * @param visible
     * @param isAllWayShowTop 是否一直显示
     */
    void onVisibilityChange(int visible,boolean isAllWayShowTop);


    void onBuyVideo();
}
