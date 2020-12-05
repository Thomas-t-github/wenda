package com.nowcoder.service;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import com.nowcoder.util.WendaUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    UserDAO userDAO;

    @Autowired
    LoginTicketDAO loginTicketDAO;

    public Map<String,Object> register(String username,String password,int expiredTime){
        HashMap<String, Object> map = new HashMap<>();

        if(StringUtils.isBlank(username)){
            map.put("msg","用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("msg","密码不能为空");
            return map;
        }

        User user = userDAO.selectByName(username);

        if (user != null){
            map.put("msg","用户名已存在");
            return map;
        }

        user = new User();

        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().replaceAll("-","").substring(0,8));
        user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setPassword(WendaUtils.MD5(password+user.getSalt()));

        userDAO.addUser(user);

        String ticket = addLoginTicket(user.getId(),expiredTime);
        if (ticket != null){
            map.put("ticket",ticket);
        }

        return map;
    }

    public Map<String,Object> login(String username,String password,int expiredTime){
        HashMap<String, Object> map = new HashMap<>();

        if(StringUtils.isBlank(username)){
            map.put("msg","用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("msg","密码不能为空");
            return map;
        }

        User user = userDAO.selectByName(username);

        if (user == null){
            map.put("msg","用户名不存在");
            return map;
        }
        if (!WendaUtils.MD5(password+user.getSalt()).equals(user.getPassword())){
            map.put("msg","密码不正确");
            return map;
        }


        String ticket = addLoginTicket(user.getId(),expiredTime);
        if (ticket != null){
            map.put("ticket",ticket);
        }

        return map;
    }

    public void logout(String ticket){
        loginTicketDAO.updateStatus(ticket,1);
    }

    public String addLoginTicket(int userId,int expiredTime){

        LoginTicket loginTicket = new LoginTicket();

        loginTicket.setUserId(userId);
        loginTicket.setStatus(0);
        loginTicket.setTicket(UUID.randomUUID().toString().replaceAll("-",""));
        Date date = new Date();
        date.setTime(date.getTime()+1000*3600*24*expiredTime);
        loginTicket.setExpired(date);

        int i = loginTicketDAO.addTicket(loginTicket);

        if (i > 0){
            return loginTicket.getTicket();
        }
        return null;
    }




    public int addUser(User user){
        return userDAO.addUser(user);
    }

    public User selectById(int id){
        return userDAO.selectById(id);
    }

    public User selectByName(String username){
        return userDAO.selectByName(username);
    }

    public int updatePassword(User user){
        return userDAO.updatePassword(user);
    }

    public int deleteById(int id){
        return userDAO.deleteById(id);
    }

}
