package com.gfan.sbbs.ui.utils;

import com.gfan.sbbs.othercomponent.MyApplication;

import android.app.Activity;
import android.widget.Toast;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class ActivityUtils {

    public static void showTips(final String str) {
        Activity activity = MyApplication.getInstance().getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyApplication.getInstance(), str, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void showTips(final int resId) {
        showTips(MyApplication.getInstance().getString(resId));
    }
}
