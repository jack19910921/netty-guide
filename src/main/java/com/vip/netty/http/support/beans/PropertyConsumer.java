package com.vip.netty.http.support.beans;

import java.beans.PropertyDescriptor;

/**
 * Created by jack on 16/8/8.
 */
public interface PropertyConsumer {
    void accept(PropertyDescriptor propertyDescriptor) throws ReflectiveOperationException;
}
