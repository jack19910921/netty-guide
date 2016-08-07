package com.vip.netty.http.support;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * Created by jack on 16/8/7.
 */
public class HttpsSupport {

    private static final String TLS = "TLS";

    public static X509TrustManager newX509TrustManager() throws Exception {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
    }

    public static SSLContext newSSLContext(X509TrustManager x509mgr)
            throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance(TLS);
        } catch (NoSuchAlgorithmException e1) {
            throw e1;
        }
        try {
            sslContext.init(null, new TrustManager[]{x509mgr}, null);
        } catch (KeyManagementException e) {
            throw e;
        }
        return sslContext;
    }
}
