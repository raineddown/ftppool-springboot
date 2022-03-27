package com.example.springbootftppool.util;

import com.example.springbootftppool.ftp.FtpClientPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;


@Slf4j
public final class FtpUtil {

	private FtpUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * 连接池
	 */
	private static FtpClientPool pool;

	/**
	 * 初始化连接池
	 *
	 * @param pool
	 */
	public static void initPool(FtpClientPool pool) {
		FtpUtil.pool = pool;
	}

	/**
	 * 切换目录
	 *
	 * @param ftpClient
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private static boolean changeWorkingDirectory(FTPClient ftpClient,
			String path) {
		boolean rst = false;
		//该部分为逐级创建
		String[] split = path.split(Matcher.quoteReplacement(File.separator));
		StringBuilder dir = new StringBuilder();
		for (String str : split) {
			if (StringUtils.isBlank(str)) {
				continue;
			}
			dir.append(File.separator).append(str);
			try {
				rst = ftpClient.changeWorkingDirectory(dir.toString());
				if (!rst) {
					ftpClient.makeDirectory(dir.toString());
					rst = ftpClient.changeWorkingDirectory(dir.toString());
				}
			} catch (IOException e) {
				log.error("切换ftp工作空间失败", e);
			}
		}
		return rst;
	}

	/**
	 * 获取文件集合
	 *
	 * @param path 路径
	 * @return
	 */
	public static FTPFile[] listFiles(String path) {
		FTPClient ftpClient = null;
		try {
			ftpClient = pool.borrowObject();
			changeWorkingDirectory(ftpClient, path);
			ftpClient.setRestartOffset(0);
			return ftpClient.listFiles();
		} catch (Exception e) {
			log.error("获取ftpclient失败", e);
		} finally {
			pool.returnObject(ftpClient);
		}
		return null;
	}

	/**
	 * 获取文件流
	 * @param path 路径
	 * @param fileName 文件名
	 * @return
	 */
	public static byte[] retrieveFileStream(String path, String fileName) {
		byte[] bytes = null;
		FTPClient ftpClient = null;
		InputStream stream = null;
		try {
			ftpClient = pool.borrowObject();
			if (changeWorkingDirectory(ftpClient, path)) {
				stream = ftpClient.retrieveFileStream(fileName);
				if (stream != null) {
					bytes =  IOUtils.toByteArray(stream);
				}
			}
		} catch (Exception e) {
			log.error("下载文件失败", e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					log.error("关闭FTP文件流失败", e);
				}
			}

			if (ftpClient != null) {
				try {

					boolean completeFlg = ftpClient.completePendingCommand();
					if (!completeFlg) {
						closeConn(ftpClient);
					}
				} catch (IOException e) {
					closeConn(ftpClient);
				}
			}
			pool.returnObject(ftpClient);
		}
		return bytes;
	}

	/**
	 * 断开FTP连接
	 * @param ftpClient
	 */
	private static void closeConn(FTPClient ftpClient) {
		try {
			ftpClient.disconnect();
		} catch (IOException ex) {
			log.error("关闭ftpClient失败", ex);
		}
	}
	
	/**
	 * 删除单一文件
	 *
	 * @param path
	 * @param fileName
	 */
	public static boolean deleteFile(String path, String fileName) {
		FTPClient ftpClient = null;
		try {
			ftpClient = pool.borrowObject();
			changeWorkingDirectory(ftpClient, path);
			ftpClient.deleteFile(fileName);
			return true;
		} catch (Exception e) {
			log.error("删除数据包失败", e);
		} finally {
			pool.returnObject(ftpClient);
		}
		return false;
	}
}
