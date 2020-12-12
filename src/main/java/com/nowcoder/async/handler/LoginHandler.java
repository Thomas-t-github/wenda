package com.nowcoder.async.handler;

import cn.hutool.extra.mail.MailUtil;
import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.model.HostHolder;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
public class LoginHandler implements EventHandler{

    @Autowired
    VelocityEngine velocityEngine;

    @Autowired
    HostHolder hostHolder;

    @Override
    public void doHandle(EventModel eventModel) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("username",eventModel.getExt("username"));
        hashMap.put("ip",eventModel.getExt("ip"));
        String result = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "mails/exception.html", "UTF-8", hashMap);

        MailUtil.send(eventModel.getExt("email"), "登录异常", result, true);

    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LOGIN);
    }
}
