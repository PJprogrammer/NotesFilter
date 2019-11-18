package com.coderboy19.notes.filter;

import android.view.View;

/**
 * Created by notes on 7/15/2017.
 */
public interface ItemClickListener {
    void onClick(View view, int position);
    void onLongClick(View view, int position);
}
