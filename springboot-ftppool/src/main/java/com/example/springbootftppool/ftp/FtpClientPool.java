package com.example.springbootftppool.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;


public class FtpClientPool extends GenericObjectPool<FTPClient> {

	public FtpClientPool(PooledObjectFactory<FTPClient> factory) {
		super(factory);
	}

	public FtpClientPool(PooledObjectFactory<FTPClient> factory, GenericObjectPoolConfig config) {
		super(factory, config);
	}

	@Override
	public void returnObject(FTPClient obj) {
		if (obj != null) {
			super.returnObject(obj);
		}
	}
}
