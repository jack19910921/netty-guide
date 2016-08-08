package com.vip.netty.http.support.util;

import com.vip.netty.http.support.enums.error.HttpErrorEnum;
import com.vip.netty.http.support.exception.HttpException;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by jack on 16/8/7.
 */
public class ReflectUtils {

    public static Map<String, String> convertJavaBean2Map(Object o) throws HttpException {

        Map<String, String> map = new TreeMap<>(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(o.getClass());
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

            if (pds != null && pds.length > 0) {
                for (PropertyDescriptor pd : pds) {
                    if (pd.getName().equals("class")) {
                        continue;
                    }
                    Object value = pd.getReadMethod().invoke(o);
                    map.put(pd.getName(), value != null ? value.toString() : "");
                }
            }

        } catch (Exception e) {
            throw new HttpException(HttpErrorEnum.REFLECT_ERROR.getErrorCode(), HttpErrorEnum.REFLECT_ERROR.getErrorMessage());
        }

        return map;
    }
}
