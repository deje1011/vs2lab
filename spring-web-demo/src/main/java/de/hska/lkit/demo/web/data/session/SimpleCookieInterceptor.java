package de.hska.lkit.demo.web.data.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Marina on 08.12.2016.
 */
public class SimpleCookieInterceptor extends HandlerInterceptorAdapter{

    @Autowired
    private StringRedisTemplate template;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Cookie[] cookies = request.getCookies();

        if(!ObjectUtils.isEmpty(cookies)){

            for(Cookie cookie : cookies){
                if(cookie.getName().equals("auth")){
                    String auth = cookie.getValue();
                    if(auth != null){
                        String uid = template.opsForValue().get("auth:" + auth + ":uid");
                        if(uid != null){
                            String name = (String) template.boundHashOps("uid:" + uid + ":user").get("name");
                            SimpleSecurity.setUser(name, uid);
                        }
                    }

                }
            }


        }
        return true;
    }


    //Clean up SimpleSession state
}
