package com.nowcoder.controller;

import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;

    @RequestMapping(path = {"/msg/detail"}, method = {RequestMethod.GET})
    public String getConversationDetail(Model model, @RequestParam("conversationId") String conversationId){

        try {
            List<Message> conversationDetail = messageService.getConversationDetail(conversationId, 0, 10);
            ArrayList<ViewObject> vos = new ArrayList<>();
            for (Message message : conversationDetail) {
                ViewObject vo = new ViewObject();
                vo.set("message",message);
                User user = userService.selectById(message.getFromId());
                vo.set("headUrl",user.getHeadUrl());
                vo.set("userId",user.getId());
                vos.add(vo);
                messageService.updateMessageRead(message.getId());
            }
            model.addAttribute("messages",vos);
        } catch (Exception e) {
            logger.error("获取私信详情失败："+e.getMessage());
        }
        return "letterDetail";
    }


    @RequestMapping(path = {"/msg/list"}, method = {RequestMethod.GET})
    public String getConversationList(Model model){

        try {
            if (hostHolder.getUser() == null){
                return "redirect:/reglogin";
            }

            List<Message> conversationList = messageService.getConversationList(hostHolder.getUser().getId(), 0, 10);
            List<ViewObject> vos = new ArrayList<>();

            for (Message message : conversationList) {
                ViewObject vo = new ViewObject();
                vo.set("message",message);
                int targetId = hostHolder.getUser().getId() == message.getFromId() ? message.getToId() : message.getFromId();
                vo.set("user",userService.selectById(targetId));
                vo.set("unread",messageService.getConvesationUnreadCount(hostHolder.getUser().getId(),message.getConversationId()));
                vo.set("messageCount",messageService.getMessageCount(message.getConversationId()));
                vos.add(vo);
            }
            model.addAttribute("conversations",vos);
        } catch (Exception e) {
            logger.error("获取消息列表失败："+e.getMessage());
        }
        return "letter";
    }

    @RequestMapping(path = {"/msg/addMessage"}, method = {RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("toName") String toName,
                             @RequestParam("content") String content){

        try {
            if (hostHolder.getUser() == null){
                return WendaUtils.getJSONString(999,"你还未登录，不能发私信！");
            }
            User user = userService.selectByName(toName);
            if (user == null){
                return WendaUtils.getJSONString(1,"用户不存在");
            }

            Message message = new Message();
            message.setContent(content);
            message.setCreatedDate(new Date());
            message.setFromId(hostHolder.getUser().getId());
            message.setToId(user.getId());
            message.setHasRead(0);
            messageService.addMessage(message);
            return WendaUtils.getJSONString(0);
        } catch (Exception e) {
            logger.error("添加私信失败："+e.getMessage());
            return WendaUtils.getJSONString(1,"增加私信失败");
        }

    }


}
