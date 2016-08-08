package com.vip.netty.http.support.beans;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;

/**
 * Created by jack on 16/8/8.
 */
public class BigDecimalEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        BigDecimal value = BigDecimal.ZERO;
        if (StringUtils.isNotEmpty(text)) {
            value = NumberUtils.createBigDecimal(text);
        }
        super.setValue(value);
    }
}
