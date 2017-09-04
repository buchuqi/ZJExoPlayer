package vodjk.com.exoplayerlib.player;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import vodjk.com.exoplayerlib.widget.ExoPlayerRootView;

/**
 * 带有预加载广告的播放器
 * Created by jian_zhou on 2017/8/16.
 */

public class ADExoPlayer extends GestureExoPlayer implements View.OnClickListener {
    public ADExoPlayer(@NonNull Activity activity, ExoPlayerRootView playerView, @NonNull String uri) {
        super(activity, playerView, uri);
        initView();
    }

    public ADExoPlayer(@NonNull Activity activity, @NonNull ExoPlayerRootView playerView) {
        super(activity, playerView);
        initView();
    }

    public ADExoPlayer(@NonNull Activity activity, @Nullable int reId) {
        super(activity, reId);
        initView();
    }

    private void initView() {
    }

    @Override
    public void onClick(View view) {

    }
}
