package com.vip.netty.http.support;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vip.netty.http.support.enums.Protocol;
import com.vip.netty.http.support.enums.RequestMethod;
import com.vip.netty.http.support.exception.HttpException;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * Created by jack on 16/7/29.
 */
//@Component
// 可作为IOC组件使用或工具类使用
public class HttpTemplate extends HttpConfigurator implements HttpOperations {

    public HttpTemplate() {
    }

    private HttpTemplate(Builder builder) {
        this.protocol = builder.protocol;
        this.contentType = builder.contentType;
        this.charSet = builder.charSet;
        this.requestMethod = builder.requestMethod;
    }

    public <T> T doPost(String url, Map<String, String> params, HttpCallback<T> action)
            throws HttpException {
        return this.execute(url, params, RequestMethod.POST, action);
    }

    public <T> T doPost(String url, Object params, HttpCallback<T> action)
            throws HttpException {
        return this.execute(url, params, RequestMethod.POST, action);
    }

    public <T> T doGet(String url, HttpCallback<T> action)
            throws HttpException {
        return this.execute(url, null, RequestMethod.GET, action);
    }


    public <T> T execute(String url, Object params, RequestMethod method, HttpCallback<T> action)
            throws HttpException {
        //convert DTO to Map
        Map<String, String> map = new HashMap<>();

        return this.execute(url, map, method, action);
    }

    public <T> T execute(String url, Map<String, String> params, RequestMethod method, HttpCallback<T> action)
            throws HttpException {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;

        try {
            //1.create http or https client
            httpclient = this.newHttpClient(protocol);

            //2.send request
            response = this.doExecute(httpclient, url, params, method);

            //3.consume response
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, charSet);

            //4.invoke callback
            return action.doParseResult(result);

        } catch (Exception e) {
            //错误码幂等
            if (e instanceof NullPointerException) {
                //TODO
                throw new HttpException();
            }
            if (e instanceof HttpException) {
                throw (HttpException) e;
            }

            e.printStackTrace();
            throw new HttpException();//TODO
        } finally {
            try {
                if (response != null) response.close();
                if (httpclient != null) httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private CloseableHttpResponse doExecute(CloseableHttpClient httpclient, String url, Map<String, String> params, RequestMethod method)
            throws Exception {

        if (method == RequestMethod.POST) {
            return doPostInternal(httpclient, url, params);
        } else if (method == RequestMethod.GET) {
            return doGetInternal(httpclient, url);
        }

        throw new UnsupportedOperationException("不支持的RequestMethod");
    }

    private CloseableHttpResponse doGetInternal(CloseableHttpClient httpclient, String url)
            throws Exception {
        HttpGet httpget = new HttpGet(url);

        return httpclient.execute(httpget);
    }

    private CloseableHttpResponse doPostInternal(CloseableHttpClient httpclient, String url, Map<String, String> params)
            throws Exception {
        HttpPost httppost = new HttpPost(url);

        List<NameValuePair> requestParams = new ArrayList<>();
        for (Entry<String, String> entry : params.entrySet()) {
            requestParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        if (requestParams.size() > 0) {
            UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(requestParams, charSet);
            httppost.setEntity(uefEntity);
        }

        return httpclient.execute(httppost);
    }

    private CloseableHttpClient newHttpClient(Protocol protocol) {
        if (protocol == Protocol.HTTP) {
            return HttpClients.createDefault();
        } else {
            return this.newHttpsClient();
        }
    }

    private CloseableHttpClient newHttpsClient() {
        return null;
    }

    public static final class Builder {

        Protocol protocol = Protocol.HTTP;
        String contentType = "application/json";
        String charSet = "UTF-8";
        RequestMethod requestMethod = RequestMethod.POST;

        public Builder protocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder charSet(String charSet) {
            this.charSet = charSet;
            return this;
        }

        public Builder requestMethod(RequestMethod requestMethod) {
            this.requestMethod = requestMethod;
            return this;
        }

        public HttpTemplate build() {
            return new HttpTemplate(this);
        }
    }
}
