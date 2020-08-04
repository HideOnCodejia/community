package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private MailClient mailClient;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextpath;
    @Autowired
    private LoginTicketMapper loginTicketMapper;
    public User findUserById(int userId){
        return  userMapper.selectById(userId);
    }

    public Map<String,Object> register(User user){
        //封装判断错误的结果，map为空才正确
        Map<String,Object> map = new HashMap<>();
        if(user == null ){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");return map;
        }
        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u!=null){
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }
        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }
        //到这里验证差不多完毕， 开始注册用户

        user.setSalt(CommunityUtil.generiteUUID().substring(0,5));
        user.setPassword(user.getPassword() + user.getSalt());
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generiteUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        //发送激活信息
        Context  context = new Context();
        context.setVariable("email",user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextpath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活用户",content);

        return map; //为空就说明成功了
    }
    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus()==1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }
    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String,Object> map = new HashMap<>();
        // 空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        //验证账号
        User user = userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg", "用户不存在!");
            return map;
        }else if(user.getStatus()==0){ //验证状态
            map.put("usernameMsg","用户未激活");
        }
        // 验证密码
        String md5password = CommunityUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(md5password)){
            map.put("passwordMsg", "密码不正确!");
            return map;
        }
        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generiteUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }
    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket,1);
    }
    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }
    public void uploadHerder(int userId,String headerUrl){
        userMapper.updateHeader(userId,headerUrl);
    }
    public void updatePassword(int userId,String password){
        userMapper.updatePassword(userId,password);
    }
}
