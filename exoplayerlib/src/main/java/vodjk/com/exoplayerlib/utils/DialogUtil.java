package vodjk.com.exoplayerlib.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import vodjk.com.exoplayerlib.R;
import vodjk.com.exoplayerlib.listener.ExoOnDialogClickListener;

/**
 * Created by jian_zhou on 2017/8/29.
 */

public class DialogUtil {

    /**
     * 创建带title、contentMsg（高度包裹内容）、2个按钮的对话框
     */
    public static Dialog createHasTitleAndWapContentDialog(Context context, String title, String message, String left, String right, boolean isHiddenCancleBtn, final ExoOnDialogClickListener listener) {
        final Dialog dialog = new Dialog(context, R.style.DialogDoubleBtn);
        dialog.setContentView(R.layout.exo_dialog_request_alpha);
        dialog.setCanceledOnTouchOutside(false);

        TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_dra_title);
        TextView tvDdbMessage = (TextView) dialog.findViewById(R.id.tv_dra_message);
        Button btnDdbLeft = (Button) dialog.findViewById(R.id.btn_dra_left);
        Button btnDdbRight = (Button) dialog.findViewById(R.id.btn_dra_right);

        tvTitle.setText(title);
        tvDdbMessage.setText(message);
        btnDdbRight.setText(right);
        //隐藏取消按钮，只显示 确定按钮
        if (isHiddenCancleBtn) {
            btnDdbLeft.setVisibility(View.GONE);
            //设置点击返回鍵以及其他区域不能使对话框消失
            dialog.setCancelable(false);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                    }
                    return false;
                }
            });
        } else {
            btnDdbLeft.setText(left);
        }

        //为两个按钮分别设置点击事件
        btnDdbLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.leftButtonClick(dialog);
            }
        });
        btnDdbRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.rightButtonClick(dialog);
            }
        });

        return dialog;
    }

}
