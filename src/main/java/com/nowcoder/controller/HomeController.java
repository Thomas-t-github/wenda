package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    UserService userService;

    @Autowired
    QuestionService questionService;

    @Autowired
    FollowService followService;

    @Autowired
    CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    private List<ViewObject> getQuestions(int userId,int offset,int limit){
        List<ViewObject> vos = new ArrayList<>();
        List<Question> questions = questionService.selectLatestQuestions(userId, offset, limit);
        for (Question question : questions) {
            ViewObject vo = new ViewObject();
            User user = userService.selectById(question.getUserId());
            vo.set("question",question);
            vo.set("followCount",followService.getFollowerCount(EntityType.ENTITY_QUESTION,question.getId()));
            vo.set("user",user);
            vos.add(vo);
        }
        return vos;
    }

    @RequestMapping(path = {"/", "/index"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model, @RequestParam(value = "pop",defaultValue = "0")int pop){

        List<ViewObject> questions = getQuestions(0, 0, 10);
        model.addAttribute("vos",questions);
        return "index";
    }

    @RequestMapping(path = {"/user/{userId}"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String userIndex(Model model, @PathVariable("userId") int userId){
        List<ViewObject> questions = getQuestions(userId, 0, 10);
        model.addAttribute("vos",questions);

        ViewObject vo = new ViewObject();

        User user = userService.selectById(userId);
        vo.set("user",user);
        vo.set("commentCount",commentService.getUserCommentCount(userId));
        vo.set("followerCount",followService.getFollowerCount(EntityType.ENTITY_USER,userId));
        vo.set("followeeCount",followService.getFolloweeCount(userId,EntityType.ENTITY_USER));

        if (hostHolder.getUser() != null){
            vo.set("followed",followService.isFollower(hostHolder.getUser().getId(),EntityType.ENTITY_USER,userId));
        }else {
            vo.set("followed",false);
        }
        model.addAttribute("profileUser",vo);

        return "profile";
    }


}
