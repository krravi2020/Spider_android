package com.app.androidkt.mqtt;

import android.view.View;

/**
 * Created by ravikumar on 16/9/17.
 */
public interface ClickListener {
    void onLongClick(View child, int childAdapterPosition);
    void onClick(View child, int childAdapterPosition);
    void onDoubleTap(View child,int childAdapterPosition );
}
