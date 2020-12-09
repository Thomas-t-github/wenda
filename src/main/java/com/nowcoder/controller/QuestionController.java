package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.*;
import com.nowcoder.service.*;
import com.nowcoder.util.WendaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class QuestionController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);


    @Autowired
    HostHolder hostHolder;

    @Autowired
    QuestionService questionService;

    @Autowired
    CommentService commentService;

    @Autowired
    UserService userService;

    @Autowired
    LikeService likeService;

    @Autowired
    FollowService followService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(value = "/question/{qid}", method = {RequestMethod.GET})
    public String questionDetail(Model model, @PathVariable("qid") int qid){
        Question question = questionService.selectById(qid);
        model.addAttribute("question",question);

        List<Comment> comments = commentService.getCommentsByEntity(question.getId(), EntityType.ENTITY_QUESTION);
        ArrayList<ViewObject> vos = new ArrayList<>();
        for (Comment comment : comments) {
            ViewObject vo = new ViewObject();
            vo.set("comment",comment);
            vo.set("user",userService.selectById(comment.getUserId()));

            if (hostHolder.getUser() == null){
                vo.set("liked",0);
            }else {
                vo.set("liked",likeService.getLikeStatus(hostHolder.getUser().getId(),EntityType.ENTITY_COMMENT,comment.getId()));
            }
            vo.set("likeCount",likeService.getLikeCount(EntityType.ENTITY_COMMENT,comment.getId()));

            vos.add(vo);
        }
        model.addAttribute("comments",vos);

        ArrayList<ViewObject> followUsers = new ArrayList<>();

        List<Integer> followerUids = followService.getFollowers(EntityType.ENTITY_QUESTION, qid, 20);
        for (Integer followerUid : followerUids) {
            ViewObject vo = new ViewObject();
            User user = userService.selectById(followerUid);
            if (user == null){
                continue;
            }
            vo.set("name",user.getName());
            vo.set("headUrl",user.getHeadUrl());
            vo.set("id",user.getId());
            followUsers.add(vo);
        }
        model.addAttribute("followUsers",followUsers);

        if (hostHolder.getUser() != null){
            model.addAttribute("followed",followService.isFollower(hostHolder.getUser().getId(),EntityType.ENTITY_QUESTION,qid));
        }else {
            model.addAttribute("followed",false);
        }

        return "detail";
    }


    @RequestMapping(value = "/question/add", method = RequestMethod.POST)
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title,@RequestParam("content") String content){

        try {
            Question question = new Question();
            question.setCommentCount(0);
            question.setCreatedDate(new Date());
            if (hostHolder.getUser() != null){
                question.setUserId(hostHolder.getUser().getId());
            }else {
                question.setUserId(WendaUtils.ANONYMOUS_USERID);
            }
            question.setTitle(title);
            question.setContent(content);
            if (questionService.addQuestion(question) > 0){

                eventProducer.fireEvent(new EventModel(EventType.ADD_QUESTION)
                .setActorId(hostHolder.getUser().getId()).setEntityId(question.getId())
                .setExt("title",question.getTitle()).setExt("content",question.getContent()));

                return WendaUtils.getJSONString(1);
            }

        } catch (Exception e) {
            logger.error("添加问题："+e.getMessage());
        }

        return WendaUtils.getJSONString(0);
    }

}
