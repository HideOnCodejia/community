package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

@Component
public class HostHolder {
    private ThreadLocal<User> threadLocal = new ThreadLocal<>();
    public void setUser(User user){
        threadLocal.set(user);
    }
    public User getUser(){
        return threadLocal.get();
    }
    public void clear() {
        threadLocal.remove();
    }
}
