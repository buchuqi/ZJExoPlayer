package vodjk.com.exoplayerlib.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.C;

import vodjk.com.exoplayerlib.R;
import vodjk.com.exoplayerlib.utils.StringUtils;
import vodjk.com.exoplayerlib.widget.ExoPlayerRootView;


/**
 * Created by yangc on 2017/2/28.
 * E-Mail:1007181167@qq.com
 * Description：增加手势播放器
 */
public class GestureExoPlayer extends BaseExoPlayer implements View.OnTouchListener {
    public static final String TAG = "GestureExoPlayer";
    //音量的最大值
    private int mMaxVolume;
    //亮度
    private float brightness = -1;
    //音量
    private int volume = -1;
    //动画
    private long newPosition = -1;
    //音量管理
    protected AudioManager audioManager;
    //控制音频和亮度布局
    private View exo_video_audio_brightness_layout;
    //显示音频和亮度布图片
    private ImageView exo_video_audio_brightness_img;
    //显示音频和亮度
    private ProgressBar exo_video_audio_brightness_pro;
    //控制进度布局
    private View exo_video_dialog_pro_layout;
    //显示进度是text
    private TextView exo_video_dialog_pro_text, exo_video_dialog_duration_text;
    //进度显示图片
    private ImageView exo_video_dialog_pro_img;
    private ProgressBar exo_video_pro;
    private GestureDetector gestureDetector;
    private int screenWidthPixels;


    public GestureExoPlayer(@NonNull Activity activity, ExoPlayerRootView playerView, @NonNull String uri) {
        super(activity, playerView, uri);
        intiView();
    }

    public GestureExoPlayer(@NonNull Activity activity, @NonNull ExoPlayerRootView playerView) {
        super(activity, playerView);
        intiView();
    }

    public GestureExoPlayer(@NonNull Activity activity, @Nullable int reId) {
        super(activity, reId);
        intiView();
    }


    private void intiView() {

        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        exo_video_audio_brightness_layout = mPlayerView.findViewById(R.id.exo_video_audio_brightness_layout);

        exo_video_audio_brightness_img = (ImageView) mPlayerView.findViewById(R.id.exo_video_audio_brightness_img);
        exo_video_audio_brightness_pro = (ProgressBar) mPlayerView.findViewById(R.id.exo_video_audio_brightness_pro);

        exo_video_dialog_pro_layout = mPlayerView.findViewById(R.id.exo_video_dialog_pro_layout);
        exo_video_pro = (ProgressBar) mPlayerView.findViewById(R.id.exo_video_pro);
        exo_video_dialog_pro_img = (ImageView) mPlayerView.findViewById(R.id.exo_video_dialog_pro_img);
        exo_video_dialog_pro_text = (TextView) mPlayerView.findViewById(R.id.exo_video_dialog_pro_text);
        exo_video_dialog_duration_text = (TextView) mPlayerView.findViewById(R.id.exo_video_dialog_duration_text);

        screenWidthPixels = mContext.getResources().getDisplayMetrics().widthPixels;
        gestureDetector = new GestureDetector(mContext, new PlayerGestureListener());
    }

    @Override
    public void onPlayVideo() {
        super.onPlayVideo();
        mPlayerView.getSimpleExoPlayerView().setOnTouchListener(this);
    }

    @Override
    void showReplay(int state) {
        if (state == View.VISIBLE) {
            mPlayerView.getSimpleExoPlayerView().setOnTouchListener(null);
        } else {
            mPlayerView.getSimpleExoPlayerView().setOnTouchListener(this);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (gestureDetector.onTouchEvent(event))
            return true;
        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
        }
        return false;
    }

    /**
     * 手势结束
     */
    @Override
    protected void endGesture() {
        volume = -1;
        brightness = -1f;
        if (newPosition >= 0) {
            simpleExoPlayer.seekTo(newPosition);
            newPosition = -1;
        }
        exo_video_audio_brightness_layout.setVisibility(View.GONE);
        exo_video_dialog_pro_layout.setVisibility(View.GONE);
    }

    /****
     * 改变进度
     *
     * @param deltaX 滑动
     **/
    @SuppressLint("SetTextI18n")
    @Override
    protected void showProgressDialog(float deltaX, String seekTime, long seekTimePosition, String totalTime, long totalTimeDuration) {
        super.showProgressDialog(deltaX, seekTime, seekTimePosition, totalTime, totalTimeDuration);
        if (seekTimePosition < totalTimeDuration / 2) {
            exo_video_dialog_pro_img.setImageResource(R.mipmap.backward_icon);
        } else {
            exo_video_dialog_pro_img.setImageResource(R.mipmap.forward_icon);
        }
        exo_video_pro.setMax((int) totalTimeDuration);
        exo_video_pro.setProgress((int) seekTimePosition);
        newPosition = seekTimePosition;
        exo_video_dialog_pro_layout.setVisibility(View.VISIBLE);
        exo_video_dialog_pro_text.setText(seekTime);
        exo_video_dialog_duration_text.setText("/" + totalTime);
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent percent 滑动
     */
    @Override
    protected void showVolumeDialog(float percent) {
        super.showVolumeDialog(percent);
        if (volume == -1) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume < 0)
                volume = 0;
        }
        int index = (int) (percent * mMaxVolume) + volume;
        if (index > mMaxVolume) {
            index = mMaxVolume;
        } else if (index < 0) {
            index = 0;
        }
        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        exo_video_audio_brightness_layout.setVisibility(View.VISIBLE);
        exo_video_audio_brightness_pro.setMax(mMaxVolume);
        exo_video_audio_brightness_pro.setProgress(index);
        exo_video_audio_brightness_img.setImageResource(index == 0 ? R.drawable.ic_volume_off_white_48px : R.drawable.ic_volume_up_white_48px);
    }

    /**
     * 滑动改变亮度
     *
     * @param percent 值大小
     */
    @Override
    protected synchronized void showBrightnessDialog(float percent) {
        if (brightness < 0) {
            brightness = mContext.getWindow().getAttributes().screenBrightness;
            if (brightness <= 0.00f) {
                brightness = 0.50f;
            } else if (brightness < 0.01f) {
                brightness = 0.01f;
            }
        }
        WindowManager.LayoutParams lpa = mContext.getWindow().getAttributes();
        lpa.screenBrightness = brightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        mContext.getWindow().setAttributes(lpa);
        if (!exo_video_audio_brightness_layout.isShown()) {
            exo_video_audio_brightness_layout.setVisibility(View.VISIBLE);
            exo_video_audio_brightness_pro.setMax(100);
            exo_video_audio_brightness_img.setImageResource(R.drawable.ic_brightness_6_white_48px);
        }
        exo_video_audio_brightness_pro.setProgress((int) (lpa.screenBrightness * 100));
    }

    /****
     * 手势监听类
     *****/
    private class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean firstTouch;
        private boolean volumeControl;
        private boolean toSeek;

        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            firstTouch = true;
            return super.onDown(e);
        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isLandScreen()) {
                float mOldX = e1.getX(), mOldY = e1.getY();
                float deltaY = mOldY - e2.getY();
                float deltaX = mOldX - e2.getX();
                if (firstTouch) {
                    toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                    volumeControl = mOldX > screenWidthPixels * 0.5f;
                    firstTouch = false;
                }
                if (toSeek) {
                    if (mediaSourceBuilder == null) return false;
                    if (!mPlayerView.isSeekbarllwayShow()) {
                        if (mediaSourceBuilder.getStreamType() == C.TYPE_HLS)
                            return super.onScroll(e1, e2, distanceX, distanceY);//直播隐藏进度条
                    }
                    deltaX = -deltaX;
                    long position = simpleExoPlayer.getCurrentPosition();
                    long duration = simpleExoPlayer.getDuration();
                    long newPosition = (int) (position + deltaX * duration / screenWidthPixels);
                    if (newPosition > duration) {
                        newPosition = duration;
                    } else if (newPosition <= 0) {
                        newPosition = 0;
                    }
                    showProgressDialog(deltaX, StringUtils.generateTime(newPosition), newPosition, StringUtils.generateTime(duration), duration);
                } else {
                    float percent = deltaY / mPlayerView.getHeight();
                    if (volumeControl) {
                        showVolumeDialog(percent);
                    } else {
                        showBrightnessDialog(percent);
                    }
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

    }

}
