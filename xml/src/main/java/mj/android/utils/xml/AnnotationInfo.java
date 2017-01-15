package mj.android.utils.xml;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

class AnnotationInfo<T> {
    public final String root;
    public final String charset;
    public final Map<String, ElementInfo> elementInfoMap = new HashMap<>();
    public final Map<String, ListAnnotationInfo<?>> listInfoMap = new HashMap<>();
    public final Class<? extends T> clazz;

    AnnotationInfo(Class<T> clazz) {
        this.clazz = clazz;
        Annotation[] annotations = clazz.getAnnotations();
        String root = null, charset = null;
        for (Annotation annotation : annotations) {
            if (annotation instanceof Root) {
                Root rootAnnotation = (Root) annotation;
                root = rootAnnotation.name();
                charset = rootAnnotation.charset();
                break;
            }
        }

        if (root == null) {
            throw new IllegalArgumentException(clazz.getName() + " : cannot find @Root annotation");
        }

        this.root = root;
        this.charset = charset.length() == 0 ? null : charset;

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            Annotation[] fieldAnnotations = field.getAnnotations();

            for (Annotation annotation : fieldAnnotations) {
                if (annotation instanceof ListContainer) {
                    String containerName = ((ListContainer) annotation).name();
                    listInfoMap.put(containerName, new ListAnnotationInfo<>((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0], field));
                } else if (annotation instanceof Element) {
                    ElementInfo elementInfo = new ElementInfo(field, (Element) annotation);
                    elementInfoMap.put(elementInfo.name, elementInfo);
                }
            }

        }

    }
}
