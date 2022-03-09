package com.example.springbootftppool.ftp;

import lombok.NoArgsConstructor;

/**
 * Description: FtpClient异常类
 *
 * @author ZouWei
 * @since 2019年11月05日
 **/
@NoArgsConstructor
public class FtpClientCreateException extends Exception {

	public FtpClientCreateException(String message) {
		super(message);
	}

	public FtpClientCreateException(String message, Throwable cause) {
		super(message, cause);
	}

	public FtpClientCreateException(Throwable cause) {
		super(cause);
	}
}
