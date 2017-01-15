package mj.android.utils.recyclerview;


import android.view.View;

public interface OnItemClickListener<VH> {
    void onItemClick(VH vh, View v);
}
