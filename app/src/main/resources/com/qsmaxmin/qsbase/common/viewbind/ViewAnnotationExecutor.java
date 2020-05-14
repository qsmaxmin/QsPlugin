package com.qsmaxmin.qsbase.common.viewbind;

import android.os.Bundle;
import android.view.View;

/**
 * 该代码由QsPlugin动态生成，拒绝外部修改（当然改了也没用）
 */
@SuppressWarnings({"WeakerAccess", "unchecked"})
public class ViewAnnotationExecutor<T> {
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
}
