package vodjk.com.exoplayerlib.listener;

/**
 * ExoPlayerView 监听
 */
public interface ExoPlayerToControl {

    void showAlertDialog();

    void setProVisible(int visibility);

    /**
     * 显示水印图片
     *
     * @param res
     */
    void setWatermarkImage(int res);

    void showSwitchName(String name);

    /**
     * 加载
     *
     * @param visibility
     */
    void setLoadStateVisible(int visibility);

    /**
     * 重播
     *
     * @param visibility
     */
    void setReplayVisible(int visibility);

    /**
     * 错误状态
     *
     * @param visibility
     */
    void setErrorStateVisible(int visibility);

    /**
     * 视频title
     *
     * @param title
     */
    void setTitle(String title);

    /**
     * 网络请求速度
     *
     * @param netSpeed
     */
    void showNetSpeed(final String netSpeed, String buffPercentage);

    /**
     * 横竖屏
     *
     * @param newConfig
     */
    void onConfigurationChanged(int newConfig);

    /**
     * 顶部返回控制栏目
     *
     * @param visibility
     */
    void showTopControlVisible(int visibility);


    void releasePlayer();

}
