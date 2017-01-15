package mj.android.utils.recyclerview;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListRecyclerAdapter<T, VH extends ListRecyclerAdapter.ViewHolder<T>> extends RecyclerView.Adapter<VH> {
    private final List<T> mDataSet;
    private OnItemClickListener<? extends VH> mListener;
    private final ViewHolderFactory<T, VH> viewHolderFactory;

    public ListRecyclerAdapter(Collection<T> collection, ViewHolderFactory<T, VH> viewHolderFactory) {
        if (collection instanceof List)
            this.mDataSet = (List<T>) collection;
        else
            mDataSet = new ArrayList<>(collection);

        this.viewHolderFactory = viewHolderFactory;
    }


    public void setOnItemClickListener(OnItemClickListener<? extends VH> listener) {
        this.mListener = listener;
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {
        h.mOnItemClickListener = mListener;

        h.item = mDataSet.get(position);

        h.setView(position);

    }

    @Override
    public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return viewHolderFactory.newViewHolder(parent, viewType);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public abstract static class ViewHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener {
        T item;
        private OnItemClickListener<ViewHolder<T>> mOnItemClickListener;

        public ViewHolder(View itemView) {
            super(itemView);
        }

        protected abstract void setView(int position);

        public final T getItem() {
            return item;
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null)
                mOnItemClickListener.onItemClick(this, v);
        }
    }

}
