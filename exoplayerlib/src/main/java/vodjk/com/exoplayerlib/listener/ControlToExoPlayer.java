package vodjk.com.exoplayerlib.listener;


import vodjk.com.exoplayerlib.player.BaseExoPlayer;


/**
 * ExoPlayer 监听
 */
public interface ControlToExoPlayer {

    void onCreatePlayers();

    void onClearPosition();

    void replayPlayers();

    void switchUri(int position, String name);

    void playVideoUri();

    void showReplayViewChange(int visibility);

    void onBack();

    BaseExoPlayer getPlay();

    //重新观看试看视频
    void onRePlayTrySee();

    /**
     * topcontro显示状态
     *
     * @param visible
     * @param allwayshows
     */
    void onshowTopControlVisible(int visible, boolean allwayshows);

    /**
     * 是否锁屏
     * @param isLockScreen
     */
    void isLockScreen(boolean isLockScreen);


}
