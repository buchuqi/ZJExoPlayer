package vodjk.com.exoplayerlib.player;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import vodjk.com.exoplayerlib.R;
import vodjk.com.exoplayerlib.widget.ExoPlayerRootView;

/**
 * 支付试看播放器
 * Created by jian_zhou on 2017/8/15
 */

public class OnlyBuyExoPlayer extends StandardExoPlayer implements View.OnClickListener {
    private FrameLayout exoOnlybuyFrame;
    //购买按钮
    private ImageView ivToPay;

    private TextView txtToTilte;

    public OnlyBuyExoPlayer(@NonNull Activity activity, @Nullable int reId) {
        super(activity, reId);
        intiView();
    }

    public OnlyBuyExoPlayer(@NonNull Activity activity, @NonNull ExoPlayerRootView playerView) {
        super(activity, playerView);
        intiView();
    }

    public OnlyBuyExoPlayer(@NonNull Activity activity, ExoPlayerRootView playerView, @NonNull String uri) {
        super(activity, playerView, uri);
        intiView();
    }

    private void intiView() {
        exoOnlybuyFrame = (FrameLayout) mPlayerView.findViewById(R.id.exo_onlybuy_frame);
        ivToPay = (ImageView) mPlayerView.findViewById(R.id.iv_to_pay);
        ivToPay.setOnClickListener(this);
        txtToTilte = (TextView) mPlayerView.findViewById(R.id.txt_to_tilte);
    }

    public void setExoOnlybuyTitle(String title) {
        txtToTilte.setText(title);
    }

    @Override
    public void onClick(View view) {
        int viewID = view.getId();
        if (viewID == R.id.iv_to_pay) {

            if (playerStateToView == null) {
                return;
            }
            playerStateToView.onBuyVideo();

        }
    }

    /**
     * 试看跟布局
     *
     * @param visi
     */
    public void setOnlyBuyRootVisi(int visi) {
        exoOnlybuyFrame.setVisibility(visi);
    }
}
