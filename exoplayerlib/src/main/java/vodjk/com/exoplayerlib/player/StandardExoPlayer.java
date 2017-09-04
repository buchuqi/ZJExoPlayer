package vodjk.com.exoplayerlib.player;

import android.app.Activity;
import android.app.Dialog;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.exoplayer2.util.Util;

import java.util.List;

import vodjk.com.exoplayerlib.R;
import vodjk.com.exoplayerlib.builder.ExoPlayerMediaSourceBuilder;
import vodjk.com.exoplayerlib.listener.ExoOnDialogClickListener;
import vodjk.com.exoplayerlib.utils.DialogUtil;
import vodjk.com.exoplayerlib.widget.ExoPlayerRootView;

/**
 * Created by yangc on 2017/2/27.
 * E-Mail:1007181167@qq.com
 * Description： 通用播放器
 */
public class StandardExoPlayer extends GestureExoPlayer {
    public static final String TAG = "StandardExoPlayer";
    private boolean isLoad = false;//已经加载
    private ImageButton exoBtn;

    public StandardExoPlayer(Activity activity, ExoPlayerRootView playerView, String uri) {
        super(activity, playerView, uri);
        intiView();
    }

    public StandardExoPlayer(Activity activity, ExoPlayerRootView playerView) {
        super(activity, playerView);
        intiView();
    }

    public StandardExoPlayer(Activity activity, int reId) {
        super(activity, reId);
        intiView();
    }

    private void intiView() {
        exoBtn = (ImageButton) mPlayerView.findViewById(R.id.exo_play);
        exoBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (exoOnDialogClickListener == null) {
                    startplayer();
                } else {
                    lessonDialog.show();
                    return true;
                }
                return false;
            }
        });
    }

    public void startplayer() {
        isLoad = true;
        createPlayers();
        hslHideView();
        registerReceiverNet();
        exoBtn.setOnTouchListener(null);
    }

    @Override
    public void setPlayUri(@NonNull Uri uri) {
        this.mediaSourceBuilder = new ExoPlayerMediaSourceBuilder(mContext.getApplicationContext(), uri);
        createPlayersNo();
    }


    @Override
    public void setADPlayUri(@NonNull String firstVideoUri, @NonNull String secondVideoUri) {
        super.setADPlayUri(firstVideoUri, secondVideoUri);
        this.mediaSourceBuilder = new ExoPlayerMediaSourceBuilder(mContext.getApplicationContext(), firstVideoUri, secondVideoUri);
        createPlayersNo();
    }

    @Override
    public void setPlaySwitchUri(@NonNull List<String> videoUri, @NonNull List<String> name, int index) {
        this.videoUri = videoUri;
        exoPlayerToControl.showSwitchName(name.get(index));
        this.mediaSourceBuilder = new ExoPlayerMediaSourceBuilder(mContext.getApplicationContext(), Uri.parse(videoUri.get(index)));
        createPlayersNo();
    }


    @Override
    public void startPlayVideo() {
        super.startPlayVideo();
    }

    @Override
    public void onResume() {
        if ((Util.SDK_INT <= 23 || simpleExoPlayer == null) && isLoad) {
            createPlayers();
        } else {
            createPlayersPlay();
        }
    }

    private boolean islooper;
    private ExoOnDialogClickListener exoOnDialogClickListener;
    private Dialog lessonDialog;

    /**
     * 配置弹出框
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
        this.islooper = islooper;
        exoOnDialogClickListener = listener;
        lessonDialog = DialogUtil.createHasTitleAndWapContentDialog(mContext, title, message, left, right, isHiddenCancleBtn, new ExoOnDialogClickListener() {
            @Override
            public void leftButtonClick(Dialog dialog) {
                dialog.dismiss();
                exoOnDialogClickListener.leftButtonClick(dialog);
            }

            @Override
            public void rightButtonClick(Dialog dialog) {
                dialog.dismiss();
                exoOnDialogClickListener.rightButtonClick(dialog);
                if (isHiddenCancleBtn) {
                    startplayer();
                }
            }
        });
    }


}
