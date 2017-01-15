package mj.android.utils.recyclerview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public final class ListRecyclerUtil {
    private ListRecyclerUtil() {
    }

    protected static abstract class ClassViewHolderFactory<T, VH extends ListRecyclerAdapter.ViewHolder<T>> implements ViewHolderFactory<T, VH> {
        private LayoutInflater inflater;
        private Constructor<VH> constructor;
        private final Class<VH> viewHolderClass;
        private final int layout;

        public ClassViewHolderFactory(Class<VH> viewHolderClass, @LayoutRes final int layout) {
            if (layout < 1)
                throw new RuntimeException("RecyclerView - ViewHolder's layout cannot be [ " + layout + " ]");
            this.viewHolderClass = viewHolderClass;
            this.layout = layout;
        }

        protected abstract Constructor<VH> getConstructor(Class<VH> viewHolderClass) throws NoSuchMethodException;

        protected abstract VH newInstance(Constructor<VH> constructor, View view) throws IllegalAccessException, InvocationTargetException, InstantiationException;

        @Override
        public final VH newViewHolder(ViewGroup parent, int viewType) {
            if (inflater == null)
                inflater = LayoutInflater.from(parent.getContext());

            try {
                if (constructor == null)
                    constructor = getConstructor(viewHolderClass);

                if (constructor == null) {
                    throw new NoSuchMethodException();
                }

                constructor.setAccessible(true);
                return newInstance(constructor, inflater.inflate(layout, parent, false));
            } catch (NoSuchMethodException e) {
                //e.printStackTrace();
                try {
                    //noinspection unchecked
                    constructor = (Constructor<VH>) viewHolderClass.getEnclosingConstructor();

                    if (constructor == null) {
                        Constructor[] constructors = viewHolderClass.getConstructors();
                        for (Constructor constructor1 : constructors) {
                            if (constructor1 != null) {
                                constructor1.setAccessible(true);
                                try {
                                    return newInstance(constructor = constructor1, inflater.inflate(layout, parent, false));
                                } catch (Exception ee) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    return newInstance(constructor, inflater.inflate(layout, parent, false));
                } catch (Exception e1) {
                    e1.printStackTrace();
                    throw new RuntimeException("cannot get constructor of Class : " + viewHolderClass.getSimpleName());
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            throw new RuntimeException("cannot call constructor of Class : " + viewHolderClass.getSimpleName());

        }
    }

    public static class InnerClassViewHolderFactory<T, VH extends ListRecyclerAdapter.ViewHolder<T>> extends ClassViewHolderFactory<T, VH> {
        private final Object outerClassObject;

        public InnerClassViewHolderFactory(Object outerClassObject, Class<VH> viewHolderClass, @LayoutRes final int layout) {
            super(viewHolderClass, layout);
            this.outerClassObject = outerClassObject;
        }

        @Override
        protected Constructor<VH> getConstructor(Class<VH> viewHolderClass) throws NoSuchMethodException {
            return viewHolderClass.getConstructor(outerClassObject.getClass(), View.class);
        }

        @Override
        protected VH newInstance(Constructor<VH> constructor, View view) throws IllegalAccessException, InvocationTargetException, InstantiationException {
            return constructor.newInstance(outerClassObject, view);
        }
    }


    public static class StaticClassViewHolderFactory<T, VH extends ListRecyclerAdapter.ViewHolder<T>> extends ClassViewHolderFactory<T, VH> {

        public StaticClassViewHolderFactory(Class<VH> viewHolderClass, @LayoutRes final int layout) {
            super(viewHolderClass, layout);
        }

        @Override
        protected Constructor<VH> getConstructor(Class<VH> viewHolderClass) throws NoSuchMethodException {
            return viewHolderClass.getConstructor(View.class);
        }

        @Override
        protected VH newInstance(Constructor<VH> constructor, View view) throws IllegalAccessException, InvocationTargetException, InstantiationException {
            return constructor.newInstance(view);
        }
    }

    public static View makeViewHolderItemView(ViewGroup viewGroup, @LayoutRes final int layout) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(layout, viewGroup, false);
    }

    public static <T, VH extends ListRecyclerAdapter.ViewHolder<T>> ListRecyclerAdapter<T, VH> newSimpleAdapter(List<T> list, final Class<VH> staticViewHolderClass, @LayoutRes final int layout) {
        return new ListRecyclerAdapter<>(list, new StaticClassViewHolderFactory<>(staticViewHolderClass, layout));
    }

    public static <T, VH extends ListRecyclerAdapter.ViewHolder<T>> ListRecyclerAdapter<T, VH> newSimpleInnerClassAdapter(List<T> list, final Object outerClassObject, final Class<VH> viewHolderClass, @LayoutRes final int layout) {
        return new ListRecyclerAdapter<>(list, new InnerClassViewHolderFactory<>(outerClassObject, viewHolderClass, layout));
    }


    public static RecyclerView.ItemDecoration squareViewItemDecoration(Context context, final int viewCount, @DimenRes int marginRes) {
        Resources res = context.getResources();
        final int screenWidth = res.getDisplayMetrics().widthPixels;
        final int marginSize = res.getDimensionPixelSize(marginRes);
        final int viewSize = (screenWidth - ((viewCount + 1) * marginSize)) / viewCount;

        final int half = marginSize / 2;
        final int lastCount = viewCount - 1;

        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = viewSize;
                params.width = viewSize;

                int position = parent.getChildAdapterPosition(view);

                int rowPosition = position % viewCount;

                if (rowPosition == 0) {
                    outRect.set(marginSize, marginSize, half, 0);
                } else if (rowPosition == lastCount) {
                    outRect.set(half, marginSize, marginSize, 0);
                } else {
                    outRect.set(half, marginSize, half, 0);
                }

                view.setLayoutParams(params);
            }
        };


    }
}
