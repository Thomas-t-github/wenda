package com.nowcoder.async.handler;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.model.EntityType;
import com.nowcoder.model.Feed;
import com.nowcoder.model.Question;
import com.nowcoder.model.User;
import com.nowcoder.service.FeedService;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class FeedHandler implements EventHandler {

    @Autowired
    UserService userService;

    @Autowired
    QuestionService questionService;

    @Autowired
    FeedService feedService;

    @Autowired
    FollowService followService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Override
    public void doHandle(EventModel eventModel) {
        Feed feed = new Feed();
        feed.setType(eventModel.getType().getValue());
        feed.setUserId(eventModel.getActorId());
        feed.setCreatedDate(new Date());
        feed.setData(buildFeedData(eventModel));
        if (feed.getData() == null){
            return;
        }
        feedService.addFeed(feed);


        //推模式
        List<Integer> followers = followService.getFollowers(EntityType.ENTITY_USER, eventModel.getActorId(), Integer.MAX_VALUE);

        followers.add(0);
        for (Integer follower : followers) {
            String timelineKey = RedisKeyUtil.getTimelineKey(follower);
            jedisAdapter.lpush(timelineKey,String.valueOf(feed.getId()));

            //限制队列长度，如果超过，则删除队列后面的新鲜事

        }

    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(new EventType[]{EventType.COMMENT,EventType.FOLLOW});
    }

    private String buildFeedData(EventModel eventModel){
        HashMap<String, String> map = new HashMap<>();

        User user = userService.selectById(eventModel.getActorId());
        if (user == null){
            return null;
        }
        map.put("userId",String.valueOf(user.getId()));
        map.put("userHead",user.getHeadUrl());
        map.put("userName",user.getName());

        if (eventModel.getType() == EventType.COMMENT ||
                (eventModel.getType() == EventType.FOLLOW &&
                        eventModel.getEntityType() == EntityType.ENTITY_QUESTION)){
            Question question = questionService.selectById(eventModel.getEntityId());
            if (question == null){
                return null;
            }
            map.put("questionId",String.valueOf(question.getId()));
            map.put("questionTitle",question.getTitle());

            return JSONObject.toJSONString(map);
        }

        return null;
    }

}
