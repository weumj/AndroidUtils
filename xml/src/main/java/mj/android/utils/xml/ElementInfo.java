package mj.android.utils.xml;

import java.lang.reflect.Field;

class ElementInfo {
    Field field;
    String name;
    boolean cdata;
    boolean require;

    public ElementInfo(Field field, Element element) {
        this.field = field;
        this.name = element.name();
        this.cdata = element.cdata();
        this.require = element.require();
    }
}
