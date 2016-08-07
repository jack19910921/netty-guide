package com.vip.netty.http.support;

import com.vip.netty.http.support.enums.Protocol;
import com.vip.netty.http.support.enums.RequestMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

/**
 * Created by jack on 16/7/31.
 */
public abstract class HttpConfigurator implements InitializingBean, ApplicationContextAware {

    protected final Log logger = LogFactory.getLog(getClass());

    protected Protocol protocol;
    protected String contentType;
    protected String charset;
    protected RequestMethod requestMethod;
    protected int connectionTime = -1;

    protected ApplicationContext applicationContext;

    public void afterPropertiesSet() {
        initConfig();

        Assert.notNull(protocol, "Protocol is required");
        Assert.notNull(contentType, "contentType is required");
        Assert.notNull(charset, "charset is required");
        Assert.notNull(requestMethod, "RequestMethod is required");
    }

    private void initConfig() {
        //load config from zk or redis or classpath
        //mock //TODO
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass("com.vip.config.support.ZkHandler");
            applicationContext.getBean(clazz);
            // TODO: 16/8/6
        } catch (ClassNotFoundException e) {
            initDefaultConfig();
        }

    }

    private void initDefaultConfig() {
        this.protocol = Protocol.HTTP;
        this.contentType = "application/json";
        this.charset = "UTF-8";
        this.requestMethod = RequestMethod.POST;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
