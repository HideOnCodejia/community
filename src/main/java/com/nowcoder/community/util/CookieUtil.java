package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {
    public static String  getValue(HttpServletRequest request,String name){
        if (request == null || name == null) {
            throw new IllegalArgumentException("参数为空!");
        }

        if(request != null && name!=null){
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
