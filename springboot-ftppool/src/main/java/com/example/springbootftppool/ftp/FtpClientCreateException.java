package com.example.springbootftppool.ftp;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FtpClientCreateException extends Exception {

	public FtpClientCreateException(String message) {
		super(message);
	}
}
