package de.hska.lkit.demo.redis.repo;

import java.util.Map;

import de.hska.lkit.demo.redis.model.User;

public interface UserRepository {
	
	/**
	 * save user to repository
	 * 
	 * @param user
	 */
	public void saveUser(User user);
	
	
	/**
	 * returns a list of all users
	 * 
	 * @return
	 */
	public Map<Object, Object> findAllUsers();
	
	
	/**
	 * find the user with username
	 * 
	 * @param username
	 * @return
	 */
	public User findUser(String username);
	
}
