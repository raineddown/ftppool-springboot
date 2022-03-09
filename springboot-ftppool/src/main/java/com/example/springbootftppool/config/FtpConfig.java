package com.example.springbootftppool.config;

import javax.annotation.PostConstruct;

import com.example.springbootftppool.ftp.FtpClientFactory;
import com.example.springbootftppool.ftp.FtpClientPool;
import com.example.springbootftppool.util.FtpUtil;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


import lombok.Data;


@Data
@Configuration
@ConfigurationProperties(prefix = "ftp")
public class FtpConfig {

    /**
     * ftp地址
     */
    private String host;

    /**
     * ftp端口
     */
    private int port;

    /**
     * ftp用户名
     */
    private String username;

    /**
     * ftp密码
     */
    private String password;

    /**
     * ftp上报路径
     */
    private String root;

    /**
     * 建立socket超时时间
     */
    private int socketTimeout;

    /**
     * 连接超时时间
     */
    private int connectTimeout;

    /**
     * 读取超时时间
     */
    private int dataTimeout;

    /**
     * 读取文件流缓存大小
     */
    private int bufferSize;

    /**
     * 线程池最大连接数
     */
    private int poolMaxActive;

    /**
     * 线程池最小空闲
     */
    private int poolMinIdle;

    /**
     * 线程池最大空闲
     */
    private int poolMaxIdle;

    /**
     * 从线程池获取连接的最大等待时间
     */
    private int poolMaxWait;

    /**
     * 检测是否空闲的时间间隔
     */
    private long checkIdleTime;

    /**
     * 连接池
     *
     */
    @PostConstruct
    public void ftpClientPool() {
        // 初始化连接池
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        config.setMaxTotal(this.poolMaxActive);
        config.setMaxIdle(this.poolMaxIdle);
        config.setMinIdle(this.poolMinIdle);
        config.setMaxWaitMillis(this.poolMaxWait);
        config.setTimeBetweenEvictionRunsMillis(this.checkIdleTime);
        FtpClientPool ftpClientPool = new FtpClientPool(
                new FtpClientFactory(this), config);
        FtpUtil.initPool(ftpClientPool);

    }
}
