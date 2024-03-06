package com.lyk.refresh_ui.utils;

import android.content.res.Resources;
import android.util.TypedValue;

public class HiDisplayUtil {
    public static int dp2px(float dp, Resources resources) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }


}
