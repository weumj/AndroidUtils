package mj.android.utils.xml;

import java.lang.reflect.Field;

class ListAnnotationInfo<T> extends AnnotationInfo<T> {
    public final Field field;

    ListAnnotationInfo(Class<T> clazz, Field field) {
        super(clazz);
        this.field = field;
    }

}
