package vodjk.com.exoplayerlib.player;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import vodjk.com.exoplayerlib.R;
import vodjk.com.exoplayerlib.widget.ExoPlayerRootView;

/**
 * 支付试看播放器
 * Created by jian_zhou on 2017/8/15
 */

public class PayExoPlayer extends StandardExoPlayer implements View.OnClickListener {
    private FrameLayout exoTryseeFrame;
    //试看按钮
    private Button btnTryseeStart;
    //试看完成
    private LinearLayout linearTryseeOver;
    //再试看一遍
    private Button btnTryseeReplay;
    //立即购买
    private Button btnTryseePay;

    public PayExoPlayer(@NonNull Activity activity, @Nullable int reId) {
        super(activity, reId);
        intiView();
    }

    public PayExoPlayer(@NonNull Activity activity, @NonNull ExoPlayerRootView playerView) {
        super(activity, playerView);
        intiView();
    }

    public PayExoPlayer(@NonNull Activity activity, ExoPlayerRootView playerView, @NonNull String uri) {
        super(activity, playerView, uri);
        intiView();
    }

    private void intiView() {
        exoTryseeFrame = (FrameLayout) mPlayerView.findViewById(R.id.exo_trysee_frame);
        btnTryseeStart = (Button) mPlayerView.findViewById(R.id.btn_trysee_start);
        btnTryseeStart.setOnClickListener(this);
        linearTryseeOver = (LinearLayout) mPlayerView.findViewById(R.id.linear_trysee_over);
        btnTryseeReplay = (Button) mPlayerView.findViewById(R.id.btn_trysee_replay);
        btnTryseeReplay.setOnClickListener(this);
        btnTryseePay = (Button) mPlayerView.findViewById(R.id.btn_trysee_pay);
        btnTryseePay.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        int viewID = view.getId();
        if (viewID == R.id.btn_trysee_start) {
            setTrySeeRootVisi(View.GONE);
            setTryseeStartVisi(View.INVISIBLE);
            startplayer();
        } else if (viewID == R.id.btn_trysee_replay) {
            startplayer();
            setTrySeeRootVisi(View.GONE);
            setTryseeOverVisi(View.INVISIBLE);
        } else if (viewID == R.id.btn_trysee_pay) {

        }
    }

    /**
     * 免费试看秒数
     */
    private long trySeeSeconds;

    /**
     * 试看秒数设置
     *
     * @param seconds
     */
    public PayExoPlayer setFreeSeeSecond(int seconds) {
        setTrySeeRootVisi(View.VISIBLE);
        trySeeSeconds = seconds * 1000;
        return this;
    }

    @Override
    protected void onPlayerCurrentPosition(long current) {
        super.onPlayerCurrentPosition(current);
        //免费试看结束
        if (current > trySeeSeconds) {
            setTrySeeRootVisi(View.VISIBLE);
            setTryseeOverVisi(View.VISIBLE);
            simpleExoPlayer.seekTo(0);
            releasePlayers();
            onBackPressed();
            if (exoPlayerToControl != null) {
                exoPlayerToControl.showTopControlVisible(View.VISIBLE);
            }
        }
    }

    /**
     * 开始试看按钮
     *
     * @param visi
     */
    private void setTryseeStartVisi(int visi) {
        btnTryseeStart.setVisibility(visi);
    }

    /**
     * 试看结束
     *
     * @param visi
     */
    private void setTryseeOverVisi(int visi) {
        linearTryseeOver.setVisibility(visi);
    }

    /**
     * 试看跟布局
     *
     * @param visi
     */
    private void setTrySeeRootVisi(int visi) {
        exoTryseeFrame.setVisibility(visi);
    }
}
