package de.hska.lkit.demo.web.data.session;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.NamedThreadLocal;

/**
 * Created by Marina on 08.12.2016.
 */
public abstract class SimpleSecurity {
    private static final ThreadLocal<UserInfo> user = new NamedThreadLocal<UserInfo>("microblog-id");

    private static class UserInfo {
        String name;
        String uid;
    }

    public static void setUser(String name, String uid){
        UserInfo userInfo = new UserInfo();
        userInfo.name = name;
        userInfo.uid = uid;
        user.set(userInfo);
    }
    public static boolean isUserSignedIn(String name){
        UserInfo userInfo = user.get();
        return userInfo != null && userInfo.name.equals(name);
    }

    public static boolean isSignedIn(){
        UserInfo userInfo = user.get();
        return userInfo != null;
    }
    public static String getName(){
        UserInfo userInfo = user.get();
        return userInfo.name;
    }

    public static String getUid(){
        UserInfo userInfo = user.get();
        return userInfo.uid;
    }
}
