package com.qsmaxmin.qsbase.common.viewbind;

import android.os.Bundle;
import android.view.View;

import com.qsmaxmin.qsbase.common.model.QsNotProguard;

/**
 * 该代码由QsPlugin动态生成，拒绝外部修改（当然改了也没用）
 */
@SuppressWarnings({"WeakerAccess", "unchecked"})
public class ViewAnnotationExecutor<T> implements QsNotProguard {
    private long lastClickTime;

    public void bindView(T target, View view) {
    }

    public void bindBundle(T target, Bundle bundle) {
    }

    public final <D extends View> D forceCastView(View view) {
        return (D) view;
    }

    public final <D> D forceCastObject(Object o) {
        return (D) o;
    }

    public boolean isFastDoubleClick(long duration) {
        if (duration <= 0) return false;
        long time = System.currentTimeMillis();
        if (time - lastClickTime < duration) {
            return true;
        } else {
            lastClickTime = time;
            return false;
        }
    }
}
