package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class FollowController {

    @Autowired
    FollowService followService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(path = {"/followUser"}, method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String followUser(@RequestParam("userId") int userId){
        if (hostHolder.getUser() == null){
            return WendaUtils.getJSONString(999);
        }

        boolean ret = followService.follow(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId);

        //异步
        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
        .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.ENTITY_USER)
        .setEntityId(userId).setEntityOwnerId(userId));

        return WendaUtils.getJSONString(ret ? 0 : 1,String.valueOf(followService.getFolloweeCount(hostHolder.getUser().getId(),EntityType.ENTITY_USER)));
    }

    @RequestMapping(path = {"/unfollowUser"}, method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String unfollowUser(@RequestParam("userId") int userId){
        if (hostHolder.getUser() == null){
            return WendaUtils.getJSONString(999);
        }

        boolean ret = followService.unfollow(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId);

        //异步

        return WendaUtils.getJSONString(ret ? 0 : 1,String.valueOf(followService.getFolloweeCount(hostHolder.getUser().getId(),EntityType.ENTITY_USER)));
    }

    @RequestMapping(path = {"/followQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String followQuestion(@RequestParam("questionId") int questionId){
        if (hostHolder.getUser() == null){
            return WendaUtils.getJSONString(999);
        }
        Question question = questionService.selectById(questionId);
        if (question == null){
            return WendaUtils.getJSONString(1,"问题不存在");
        }
        boolean ret = followService.follow(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, questionId);

        //异步
        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
        .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.ENTITY_QUESTION)
        .setEntityId(questionId).setEntityOwnerId(question.getUserId()));

        HashMap<String, Object> info = new HashMap<>();
        info.put("headUrl",hostHolder.getUser().getHeadUrl());
        info.put("name",hostHolder.getUser().getName());
        info.put("id",hostHolder.getUser().getId());
        info.put("count",followService.getFollowerCount(EntityType.ENTITY_QUESTION,questionId));

        return WendaUtils.getJSONString(ret ? 0 : 1,info);
    }

    @RequestMapping(path = {"/unfollowQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String unfollowQuestion(@RequestParam("questionId") int questionId){
        if (hostHolder.getUser() == null){
            return WendaUtils.getJSONString(999);
        }
        Question question = questionService.selectById(questionId);
        if (question == null){
            return WendaUtils.getJSONString(1,"问题不存在");
        }
        boolean ret = followService.unfollow(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, questionId);

        //异步

        HashMap<String, Object> info = new HashMap<>();

        info.put("id",hostHolder.getUser().getId());
        info.put("count",followService.getFollowerCount(EntityType.ENTITY_QUESTION,questionId));

        return WendaUtils.getJSONString(ret ? 0 : 1,info);
    }

    @RequestMapping(path = {"/user/{uid}/followers"}, method = {RequestMethod.GET})
    public String followers(Model model, @PathVariable("uid") int userId){
        List<Integer> followerIds = followService.getFollowers(EntityType.ENTITY_USER, userId, 0, 10);
        if (hostHolder.getUser() != null){
            model.addAttribute("followers",getUsersInfo(hostHolder.getUser().getId(),followerIds));
        }else {
            model.addAttribute("followers",getUsersInfo(0,followerIds));
        }
        model.addAttribute("followerCount",followService.getFollowerCount(EntityType.ENTITY_USER,userId));
        model.addAttribute("curUser",userService.selectById(userId));
        return "followers";
    }

    @RequestMapping(path = {"/user/{uid}/followees"}, method = {RequestMethod.GET})
    public String followees(Model model, @PathVariable("uid") int userId){
        List<Integer> followeeIds = followService.getFollowees(userId,EntityType.ENTITY_USER, 0, 10);
        if (hostHolder.getUser() != null){
            model.addAttribute("followees",getUsersInfo(hostHolder.getUser().getId(),followeeIds));
        }else {
            model.addAttribute("followees",getUsersInfo(0,followeeIds));
        }
        model.addAttribute("followeeCount",followService.getFolloweeCount(userId,EntityType.ENTITY_USER));
        model.addAttribute("curUser",userService.selectById(userId));
        return "followees";
    }

    private List<ViewObject> getUsersInfo(int localUserId,List<Integer> userIds){

        ArrayList<ViewObject> userInfos = new ArrayList<>();
        for (Integer userId : userIds) {
            User user = userService.selectById(userId);
            if (user == null){
                continue;
            }
            ViewObject vo = new ViewObject();
            vo.set("user",user);
            vo.set("commentCount",commentService.getUserCommentCount(userId));
            vo.set("followerCount",followService.getFollowerCount(EntityType.ENTITY_USER,userId));
            vo.set("followeeCount",followService.getFolloweeCount(userId,EntityType.ENTITY_USER));
            if (localUserId != 0){
                vo.set("followed",followService.isFollower(localUserId,EntityType.ENTITY_USER,userId));
            }else {
                vo.set("followed",false);
            }
            userInfos.add(vo);
        }
        return userInfos;
    }


    @RequestMapping(path = {"/followers"}, method = {RequestMethod.GET,RequestMethod.POST})
    public String followers(){
        return "followers";
    }


    @RequestMapping(path = {"/followees"}, method = {RequestMethod.GET,RequestMethod.POST})
    public String followees(){
        return "followees";
    }

}
