package com.vip.netty.http.support.util;

import com.google.common.collect.Maps;
import com.vip.netty.http.support.beans.BigDecimalEditor;
import com.vip.netty.http.support.beans.PropertyConsumer;
import com.vip.netty.http.support.enums.error.HttpErrorEnum;
import com.vip.netty.http.support.exception.HttpException;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.beans.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

/**
 * Created by jack on 16/8/7.
 */
public class ReflectUtils {

    private final static Map<String, Object> reflectKVMap = Maps.newHashMapWithExpectedSize(Byte.MAX_VALUE);

    static {
        PropertyEditorManager.registerEditor(BigDecimal.class, BigDecimalEditor.class);
    }

    public static boolean isPrimitiveWrapper(Class clazz) {
        if (clazz == null) {
            return false;
        }
        return clazz.isPrimitive()
                || clazz.isAssignableFrom(String.class)
                || Number.class.isAssignableFrom(clazz);
    }

    public static void forEach(Class clazz, PropertyConsumer action) {
        forEach(clazz, action, false);
    }

    public static void forEach(Class clazz, PropertyConsumer action, boolean ignoreException) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        PropertyDescriptor[] pds = propertyUtilsBean.getPropertyDescriptors(clazz);

        if (ArrayUtils.isNotEmpty(pds)) {
            for (PropertyDescriptor pd : pds) {
                String name = pd.getName();
                if (name.equals("class")) {
                    continue;
                }

                try {
                    action.accept(pd);
                } catch (ReflectiveOperationException e) {
                    if (!ignoreException) {
                        throw new UnsupportedOperationException(ExceptionUtils.getRootCauseMessage(e));
                    }
                }

            }
        }
    }

    public static <T> T getPrimitiveObject(String primitiveStr, Class<T> clazz) {
        PropertyEditor propertyEditor = PropertyEditorManager.findEditor(clazz);
        propertyEditor.setAsText(primitiveStr);
        return (T) propertyEditor.getValue();
    }

    public static Field[] getAllFields(final Class clazz) {
        return (Field[]) get(clazz.getName(), new ValueSupport() {
            @Override
            public Object get(String key) {
                return FieldUtils.getAllFields(clazz);
            }
        });
    }

    public static Field getField(final Class clz, final String name) {
        String key = clz.getName() + "#" + name;
        return (Field) get(key, new ValueSupport() {
            @Override
            public Object get(String key) {
                for (Field field : getAllFields(clz)) {
                    if (StringUtils.equals(field.getName(), name)) {
                        return field;
                    }
                }
                return null;
            }
        });
    }

    public static <T extends Annotation> T getFieldAnnotation(final Class clz, final String fieldName, final Class<T> annotationClass) {
        String key = clz.getName() + "#" + fieldName + "@" + annotationClass.getName();
        return (T) get(key, new ValueSupport() {
            @Override
            public Object get(String key) {
                Field field = getField(clz, fieldName);
                if (field != null) {
                    return field.getAnnotation(annotationClass);
                }
                return null;
            }
        });
    }

    private static synchronized Object get(String key, ValueSupport vs) {
        Object v = reflectKVMap.get(key);
        if (v != null)
            return v;
        v = vs.get(key);
        reflectKVMap.put(key, v);
        return v;
    }

    private static interface ValueSupport {
        Object get(String key);
    }

    public static Map<String, String> convertJavaBean2Map(Object o) throws HttpException {

        Map<String, String> map = Maps.newTreeMap(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(o.getClass());
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

            if (ArrayUtils.isNotEmpty(pds)) {
                for (PropertyDescriptor pd : pds) {
                    if (pd.getName().equals("class")) {
                        continue;
                    }
                    Object value = pd.getReadMethod().invoke(o);
                    map.put(pd.getName(), value != null ? value.toString() : "");
                }
            }

        } catch (Exception e) {
            throw new HttpException(HttpErrorEnum.REFLECT_ERROR.getErrorCode(),
                    HttpErrorEnum.REFLECT_ERROR.getErrorMessage());
        }

        return map;
    }
}
