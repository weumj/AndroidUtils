package mj.android.utils.recyclerview;

import android.view.ViewGroup;

public interface ViewHolderFactory<T, VH extends ListRecyclerAdapter.ViewHolder<T>> {
    VH newViewHolder(ViewGroup parent, int viewType);
}
