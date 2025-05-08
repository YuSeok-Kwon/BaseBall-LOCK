package com.kepg.BaseBallLOCK.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.web.multipart.MultipartFile;

public class FileManager {
	
	// 같은 목적을 가진 코드가 중복되면 유지보수에 좋지 않음(WebMvcConfig 에서도 이 경로 사용)
	// 멤버변수로 사용하여 다른 Class에서도 사용 할 수 있게 하자
	// static을 부여해서 객체생성 없이 사용 할 수 있게 하자
	// 경로는 바뀌면 안되기 떄문에 final사용(final은 보통 대문자)
	public static final String FILE_UPLOAD_PATH = "/home/ec2-user/upload/BaseBallLOCK";
	
	//파일 저장기능
	public static String saveFile(int userId, MultipartFile file) {
		
		if(file == null) {
			return null;
		}
		
		// 파일 이름 유지
		// 폴더를 생성해서 파일을 저장
		// 사용자 정보(PK)를 폴더이름으로 사용한다
		// 시간 정보를 포함
		// UNIX TIME : 1970년 1월 1일 0시 0분 0초 부터 흐른 시간을 milli second(1/1000초) 단위로 표현한 값
		// ex) 5_345223434
		
		// 사용자별 폴더 생성
		String directoryName = "/" + userId + "_" + System.currentTimeMillis();
		
		// 디렉토리(폴더) 만들기 (파일 저장 폴더 경로 + 사용자별 폴더 위치 경로)
		String directoryPath = FILE_UPLOAD_PATH + directoryName;
		
		// 디렉토리 생성
		File directory = new File(directoryPath);
		
		if(!directory.mkdir()) {
			// 디렉토리 생성 실패
			return null;
		}
		
		// 생성한 디렉토리에 파일 저장 (디렉토리 경로 + 파일 이름으로 된 폴더
		String filePath = directoryPath + "/" + file.getOriginalFilename();
		
		try {
			byte[] bytes = file.getBytes();
			
			// 경로를 관리하는 객체 생성
			Path path = Paths.get(filePath);
			
			// 저장할 경로의 Path객체, bytes 배열 저장
			Files.write(path, bytes);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
			//파일 저장 실패
			return null;
		}
		
		// 실제 파일 저장위치와 url 경로를 매칭하는 규칙
		// /Users/yu_seok/Documents/workspace/Spring/upload/Memo
		// /images/
		
		// /Users/yu_seok/Documents/workspace/Spring/upload/Memo/1_132900923/image.png
		// /images/1_132900923/image.png
		
		// /(슬래쉬) 신경 쓰기
		return "images" + directoryName + "/" + file.getOriginalFilename();
	}
	
	// 파일 삭제기능
	public static boolean removeFile(String imagePath) {
		
		if(imagePath == null) {
			return false;
		}
		
		// /images/6_1742647610870/IMG_7284.JPG
		
		String fullFilePath = FILE_UPLOAD_PATH + imagePath.replace("/images", "");
		
		// 경로를 관리하는 객체 생성
		Path path = Paths.get(fullFilePath);
		
		try {
			// 파일 삭제
			Files.delete(path);
			
			// 폴더 삭제
			// /Users/yu_seok/Documents/workspace/Spring/upload/Memo/1_132900923/
			Path directoryPath = path.getParent(); // path의 상위 경로 리턴
			Files.delete(directoryPath);
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}
