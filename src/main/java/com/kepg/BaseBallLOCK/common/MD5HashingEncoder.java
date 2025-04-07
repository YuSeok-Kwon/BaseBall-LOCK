package com.kepg.BaseBallLOCK.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5HashingEncoder {

	// md5를 통한 암호화
	public static String encode(String message) {
		
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("md5");
			
			// 암호화를 위해 byte 배열로 변경
			byte[] bytes = message.getBytes();
			
			// 암호화 진행
			messageDigest.update(bytes);
			
			// 암호화가 완료된 후 byte 배열로 다시 저장
			byte[] digest = messageDigest.digest();
			
			String result = "";
			// byte를 16진수로 변경
			for(int i = 0; i<digest.length; i++) {
				// byte 연산
				result += Integer.toHexString(digest[i] & 0xff);
			}
			
			return result;
			
		} catch (NoSuchAlgorithmException e) {
			
			e.printStackTrace();
			
			return null;
		}
		
	}
}
