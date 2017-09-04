package vodjk.com.zjexoplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.ExoPlaybackException;

import vodjk.com.exoplayerlib.listener.ExoPlayerType;
import vodjk.com.exoplayerlib.listener.PlayerStateToView;
import vodjk.com.exoplayerlib.widget.ExoPlayerView;

public class MainActivity extends AppCompatActivity {
    private LinearLayout content;
    private ExoPlayerView exoPlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        content = (LinearLayout) findViewById(R.id.content);

        exoPlayerView = (ExoPlayerView) findViewById(R.id.exo_play_context_id);
        exoPlayerView.initPlayer(ExoPlayerType.Standard)
                .setPlayUri("http://vodcdn.yst.vodjk.com/201709041819/26c6e98559fb97c51b28bfbc94702fc2/company/1/2016/12/16/1012157qqxpx2cwl2rtipjsjor/sd/9b9368ccccda4a55bc1234ad5e13cd61.m3u8")
                .loadArtWork("http://i3.letvimg.com/lc08_yunzhuanma/201707/29/20/49/3280a525bef381311b374579f360e80a_v2_MTMxODYyNjMw/thumb/2_960_540.jpg")
                .isAutoLandScreen(true)
                .isLockScreen(true)
                .setonPlayerStateListener(new PlayerStateToView() {

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
        exoPlayerView.onConfigurationChanged(newConfig, content);//横竖屏切换
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        exoPlayerView.onBackPressed();//使用播放返回键监听
    }


}
