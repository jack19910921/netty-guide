package com.vip.netty.http.support;

import java.io.IOException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vip.netty.http.support.enums.Order;
import com.vip.netty.http.support.enums.Protocol;
import com.vip.netty.http.support.enums.RequestMethod;
import com.vip.netty.http.support.enums.error.HttpErrorEnum;
import com.vip.netty.http.support.exception.HttpException;
import com.vip.netty.http.support.util.ReflectUtils;
import com.vip.netty.http.support.util.SignUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

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
        this.charset = builder.charset;
        this.requestMethod = builder.requestMethod;
        this.order = builder.order;
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

        Map<String, String> map = ReflectUtils.convertJavaBean2Map(params);

        return this.execute(url, map, method, action);
    }

    public <T> T execute(String url, Map<String, String> params, RequestMethod method, HttpCallback<T> action)
            throws HttpException {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;

        try {
            //1.sort params and build sign
            Map<String, String> sortedMapWithSign = this.sign(params, order, charset);

            //2.create http or https client
            httpclient = this.newHttpClient(protocol);

            //3.send request
            response = this.doExecute(httpclient, url, sortedMapWithSign, method);

            //4.consume response
            if (response == null || response.getEntity() == null) {
                throw new HttpException(HttpErrorEnum.RESPONSE_IS_EMPTY.getErrorCode(),
                        HttpErrorEnum.RESPONSE_IS_EMPTY.getErrorMessage());
            }

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new HttpException(HttpErrorEnum.RESPONSE_STATUS_CODE_INVALID.getErrorCode(),
                        HttpErrorEnum.RESPONSE_STATUS_CODE_INVALID.getErrorMessage());
            }

            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, charset);

            //5.invoke callback
            return action.doParseResult(result);

        } catch (Exception e) {
            if (e instanceof HttpException) {
                throw (HttpException) e;
            }
            throw new HttpException(HttpErrorEnum.SYSTEM_INTERNAL_ERROR.getErrorCode(),
                    HttpErrorEnum.SYSTEM_INTERNAL_ERROR.getErrorMessage());
        } finally {
            try {
                if (response != null) response.close();
                if (httpclient != null) httpclient.close();
            } catch (IOException e) {
                throw new HttpException(HttpErrorEnum.CLOSE_CHANNEL_ERROR.getErrorCode(),
                        HttpErrorEnum.CLOSE_CHANNEL_ERROR.getErrorMessage());
            }
        }
    }

    private Map<String, String> sign(Map<String, String> params, final Order order, String charset) {
        Map<String, String> map = Maps.newLinkedHashMap();

        if (order == Order.IMMUTABLE) {
            map.putAll(params);
        } else {
            Map<String, String> treeMap = Maps.newTreeMap(new Comparator<String>() {

                @Override
                public int compare(String o1, String o2) {
                    if (order == Order.ASC) {
                        return o1.compareTo(o2);
                    } else {
                        return o2.compareTo(o1);
                    }
                }
            });
            map.putAll(treeMap);
        }

        String sign = SignUtil.sign(map, charset);
        map.put("sign", sign);// TODO: 16/8/9  
        
        return map;
    }

    private CloseableHttpResponse doExecute(CloseableHttpClient httpclient, String url, Map<String, String> params, RequestMethod method)
            throws Exception {
        if (method == RequestMethod.POST) {
            return doPostInternal(httpclient, url, params);
        } else if (method == RequestMethod.GET) {
            return doGetInternal(httpclient, url);
        }

        throw new HttpException(HttpErrorEnum.UNSUPPORTED_REQUEST_METHOD.getErrorCode(),
                HttpErrorEnum.UNSUPPORTED_REQUEST_METHOD.getErrorMessage());
    }

    private CloseableHttpResponse doGetInternal(CloseableHttpClient httpclient, String url)
            throws Exception {
        HttpGet httpGet = new HttpGet(url);

        return httpclient.execute(httpGet);
    }

    private CloseableHttpResponse doPostInternal(CloseableHttpClient httpclient, String url, Map<String, String> params)
            throws Exception {
        HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> requestParams = Lists.newArrayList();
        for (Entry<String, String> entry : params.entrySet()) {
            requestParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        if (requestParams.size() > 0) {
            UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(requestParams, charset);
            httpPost.setEntity(uefEntity);
        }

        return httpclient.execute(httpPost);
    }

    private CloseableHttpClient newHttpClient(Protocol protocol) throws Exception {
        if (protocol == Protocol.HTTP) {
            return HttpClients.createDefault();
        } else {
            return this.newHttpsClient();
        }
    }

    private CloseableHttpClient newHttpsClient() throws Exception {
        X509TrustManager x509mgr = HttpsSupport.newX509TrustManager();
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(connectionTime)
                .setConnectTimeout(connectionTime)
                .build();

        SSLContext sslContext = HttpsSupport.newSSLContext(x509mgr);
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        return HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public static final class Builder {

        Protocol protocol = Protocol.HTTP;
        String contentType = "application/json";
        String charset = "UTF-8";
        RequestMethod requestMethod = RequestMethod.POST;
        Order order = Order.ASC;

        public Builder protocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder charSet(String charSet) {
            this.charset = charSet;
            return this;
        }

        public Builder requestMethod(RequestMethod requestMethod) {
            this.requestMethod = requestMethod;
            return this;
        }

        public Builder order(Order order) {
            this.order = order;
            return this;
        }

        public HttpTemplate build() {
            return new HttpTemplate(this);
        }
    }
}
