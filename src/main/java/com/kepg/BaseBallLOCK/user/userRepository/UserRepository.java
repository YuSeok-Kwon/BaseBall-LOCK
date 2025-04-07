package com.kepg.BaseBallLOCK.user.userRepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kepg.BaseBallLOCK.user.userDomain.User;

public interface UserRepository extends JpaRepository<User, Integer>{

	public User findByLoginIdAndPassword(String loginId, String password);
	
	public int countByLoginId(String loginId);
	
	public int countByNickname(String nickname);
	
	public Optional<User> findByNameAndEmail(String name, String email);
	
	public User findByLoginIdAndNameAndEmail(String loginId, String name, String email);
	
	public Optional<User> findByLoginId(String loginId);
}
