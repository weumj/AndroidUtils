package mj.android.utils.xml;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class XmlParser<T> {
    private Class<T> clazz;
    private AnnotationInfo<T> annotationInfo;

    public XmlParser(Class<T> clazz) {
        this.clazz = clazz;
    }

    public XmlParser(Class<T> clazz, boolean readAnnotationOnInit) {
        this.clazz = clazz;
        if (readAnnotationOnInit) initAnnotationInfo();
    }

    private void initAnnotationInfo() {
        if (annotationInfo == null) {
            synchronized (this) {
                if (annotationInfo == null) {
                    annotationInfo = new AnnotationInfo<>(clazz);
                }
            }
        }
    }

    public T parse(InputStream is) throws Exception {
        initAnnotationInfo();

        try {
            XmlPullParser pullParser = XmlPullParserFactory.newInstance().newPullParser();

            pullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            pullParser.setInput(is, annotationInfo.charset);
            pullParser.nextTag();

            return parseContent(pullParser, annotationInfo);

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //ignored
                }
            }
        }

    }

    protected static <T> T parseContent(XmlPullParser parser, AnnotationInfo<T> annotationInfo) throws IOException, XmlPullParserException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        parser.require(XmlPullParser.START_TAG, null, annotationInfo.root);

        T item = newInstance(annotationInfo.clazz);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (annotationInfo.elementInfoMap.containsKey(name)) {
                ElementInfo info = annotationInfo.elementInfoMap.get(name);
                readElement(parser, info, item);
            } else if (annotationInfo.listInfoMap.containsKey(name)) {
                parseList(parser, annotationInfo.listInfoMap.get(name), item);
            } else {
                XmlParserUtil.skip(parser);
            }

        }

        return item;
    }

    private static <T> void parseList(XmlPullParser parser, ListAnnotationInfo<T> annotationInfo, Object item) throws IOException, XmlPullParserException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Field field = annotationInfo.field;
        ArrayList<T> list = new ArrayList<>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            parser.require(XmlPullParser.START_TAG, null, annotationInfo.root);
            T listItem = newInstance(annotationInfo.clazz);

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                String tagName = parser.getName();
                if (annotationInfo.elementInfoMap.containsKey(tagName)) {
                    ElementInfo info = annotationInfo.elementInfoMap.get(tagName);
                    readElement(parser, info, listItem);
                } else {
                    XmlParserUtil.skip(parser);
                }

            }

            list.add(listItem);

        }

        field.set(item, list);
    }


    private static <T> void readElement(XmlPullParser parser, ElementInfo elementInfo, T item) throws IOException, XmlPullParserException {
        String tagName = parser.getName();
        if (elementInfo.name.equals(tagName)) {
            setObjectData(parser, elementInfo, item);
        }
    }


    private static boolean setObjectData(XmlPullParser parser, ElementInfo elementInfo, Object object) {
        Field f = elementInfo.field;
        Class<?> type = f.getType();

        String text;

        try {
            text = XmlParserUtil.readText(parser);
            if (elementInfo.cdata && text != null)
                text = removeCDATA(text);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return false;
        }

        switch (type.getSimpleName()) {
            case "String":
                try {
                    f.set(object, text == null ? "" : text);
                    return true;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;

            case "int":
                try {
                    f.setInt(object, text == null ? 0 : Integer.parseInt(text));
                    return true;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;

            case "long":
                try {
                    f.setLong(object, text == null ? 0 : Long.parseLong(text));
                    return true;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;

            case "float":
                try {
                    f.setFloat(object, text == null ? 0 : Float.parseFloat(text));
                    return true;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;

            case "double":
                try {
                    f.setDouble(object, text == null ? 0 : Double.parseDouble(text));
                    return true;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
        return false;

    }

    private static final String CDATA_HEAD = "<![CDATA[";
    private static final String CDATA_TAIL = "]]>";

    protected static String removeCDATA(String in) {
        if (in == null)
            return null;

        return in.replace(CDATA_HEAD, "").replace(CDATA_TAIL, "").trim();
    }

    private static <T> T newInstance(Class<? extends T> clazz) throws InstantiationException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        T item;
        try {
            item = clazz.newInstance();
        } catch (IllegalAccessException e) {
            Constructor<? extends T> constructor = clazz.getDeclaredConstructor();
            if (!constructor.isAccessible())
                constructor.setAccessible(true);
            item = constructor.newInstance();
        }

        return item;
    }

}
