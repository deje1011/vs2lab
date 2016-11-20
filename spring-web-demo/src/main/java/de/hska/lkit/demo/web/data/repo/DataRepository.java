package de.hska.lkit.demo.web.data.repo;

import de.hska.lkit.demo.web.data.model.User;

/**
 * Interface to specify interactions with database.
 *
 * Created by Marina on 20.11.2016.
 */
public interface DataRepository {

    /**
     * Adds user to db.
     * @param user user to add
     */
    public void addUser(User user);

    /**
     * Compares passwords.
     * @param name name of user to check password for
     * @param password password entered
     * @return true if password is valid
     */
    public boolean isPasswordValid(String name, String password);

    /**
     * Get the id of a certain user
     * @param name name of user
     * @return id of user as string
     */
    public String getUserId(String name);
}
