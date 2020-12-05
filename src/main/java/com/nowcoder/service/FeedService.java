package com.nowcoder.service;

import com.nowcoder.dao.FeedDAO;
import com.nowcoder.model.Feed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedService {

    @Autowired
    FeedDAO feedDAO;

    public boolean addFeed(Feed feed){
        return feedDAO.addFeed(feed) > 0;
    }

    public Feed selectFeedById(int id){
        return feedDAO.selectFeedById(id);
    }

    public List<Feed> selectUserFeeds(int maxId, List<Integer> userIds, int count){
        return feedDAO.selectUserFeeds(maxId,userIds,count);
    }

}
