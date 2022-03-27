package com.example.springbootftppool.ftp;

import com.example.springbootftppool.config.FtpConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class FtpClientFactory extends BasePooledObjectFactory<FTPClient> {

	private Logger logger = LoggerFactory.getLogger(FtpClientFactory.class);

	private FtpConfig config;

	public FtpClientFactory(FtpConfig config) {
		this.config = config;
	}

	@Override
	public FTPClient create() throws Exception {
		FTPClient client = new FTPClient();
		boolean hasError = false;
		try {
			client.setDefaultPort(config.getPort());
			client.setConnectTimeout(config.getConnectTimeout());
			client.setDataTimeout(config.getDataTimeout());
			client.setDefaultTimeout(config.getSocketTimeout());
			client.connect(config.getHost(), config.getPort());

			long startTime = System.currentTimeMillis();
			if (!client.login(config.getUsername(), config.getPassword())) {
				hasError = true;
				String replyStr = client.getReplyString();
				client.logout();
				throw new FtpClientCreateException("登录FTP失败！" + replyStr);
			}

			//检测登录是否成功
			if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
				hasError = true;
				String replyStr = client.getReplyString();
				client.disconnect();
				throw new FtpClientCreateException("FTP拒绝连接!" + replyStr);

			}
			client.setFileType(FTP.BINARY_FILE_TYPE);
			client.enterLocalPassiveMode();
			client.setBufferSize(config.getBufferSize());
			logger.info("ftp【{}】链接创建成功，用时：{}毫秒", config.getHost(),
					(System.currentTimeMillis() - startTime));
			return client;
		} finally {
			// 如果发生异常，关闭已经创建的连接
			if (hasError) {
				closeCon(client);
			}
		}
	}

	@Override
	public PooledObject<FTPClient> wrap(FTPClient ftpClient) {
		return new DefaultPooledObject<>(ftpClient);
	}

	@Override
	public void destroyObject(PooledObject<FTPClient> p) {
		closeCon(p.getObject());
		logger.info("ftp【{}】连接销毁成功！", config.getHost());
	}

	@Override
	public boolean validateObject(PooledObject<FTPClient> p) {
		boolean valid = false;
		try {
			FTPClient client = p.getObject();
			if (client != null && client.isConnected()) {
				valid = client.sendNoOp();
			}
		} catch (IOException e) {
			logger.debug("验证FTPClient失败", e);
		}
		return valid;
	}

	/**
	 * 销毁ftp连接
	 *
	 * @param ftpClient
	 */
	private void closeCon(FTPClient ftpClient) {

		if (ftpClient == null) {
			return;
		}
		if (ftpClient.isConnected()) {
			try {
				ftpClient.disconnect();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}

	/**
	 * ftp连接钝化，还连接时将ftpclient工作空间置根目录
	 */
	@Override
	public void passivateObject(PooledObject<FTPClient> p) throws Exception {
		FTPClient client = p.getObject();
		client.changeWorkingDirectory("/");
	}
}
