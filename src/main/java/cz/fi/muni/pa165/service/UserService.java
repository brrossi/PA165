package cz.fi.muni.pa165.service;

import java.util.Collection;
import java.util.List;

import cz.fi.muni.pa165.dto.UserDTO;
import cz.fi.muni.pa165.entity.Product;
import cz.fi.muni.pa165.entity.User;

/**
 * An interface that defines a service access to the {@link User} entity.
 */
public interface UserService {
	/**
	 * Register the given user that has provided hash password.
	 * @param u
	 */
	void registerUser(UserDTO u);

	/**
	 * Register the given user with the given unencrypted password.
	 */
	void registerUser(UserDTO u, String unencryptedPassword);

	/**
	 * Get all registered users
	 */
	Collection<UserDTO> getAllUsers();

	/**
	 * Try to authenticate a user. Return true only if the hashed password matches the records.
	 */
	boolean authenticate(UserDTO u, String password);

	/**
	 * Check if the given user is admin.
	 */
	boolean isAdmin(UserDTO u);
}