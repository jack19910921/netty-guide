package com.vip.netty.http.support.test;

import com.google.common.collect.Maps;
import com.vip.netty.http.support.HttpCallback;
import com.vip.netty.http.support.HttpTemplate;
import com.vip.netty.http.support.enums.Protocol;
import com.vip.netty.http.support.enums.RequestMethod;
import com.vip.netty.http.support.exception.HttpException;

import java.util.Map;

/**
 * Created by jack on 16/8/6.
 */
public class HttpTest {

    public static void main(String[] args) throws HttpException {
        HttpTemplate httpTemplate = new HttpTemplate.Builder()
                .charSet("UTF-8")
                .protocol(Protocol.HTTP)
                .build();

        Map<String, String> response = httpTemplate.execute("url", Maps.<String, String>newHashMap(), RequestMethod.POST,

                new HttpCallback<Map<String, String>>() {

                    @Override
                    public Map<String, String> doParseResult(String result) throws HttpException {
                        return null;
                    }
                });
    }

}
