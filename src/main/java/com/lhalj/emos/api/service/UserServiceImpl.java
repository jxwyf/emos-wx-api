package com.lhalj.emos.api.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lhalj.emos.api.db.dao.TbUserDao;
import com.lhalj.emos.api.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * 描述:
 */
@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService{

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    @Autowired
    private TbUserDao userDao;

    private String getOpenId(String code){
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap map = new HashMap();
        map.put("appid",appId);
        map.put("secret",appSecret);
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        //返回值
        String response  = HttpUtil.post(url, map);
        JSONObject json = JSONUtil.parseObj(response);
        String openId = json.getStr("openid");
        if (openId==null || openId.length() == 0) {
            throw new RuntimeException("临时登录凭证错误");
        }
        return openId;
    }

    @Override
    public int registerUser(String registerCode, String code, String nickname, String photo) {
        //注册超级管理员
        if (registerCode.equals("000000")) {
            boolean bool = userDao.haveRootUser();
            if(!bool){
                String openId = getOpenId(code);
                HashMap param = new HashMap();
                param.put("openId",openId);
                param.put("nickname",nickname);
                param.put("photo",photo);
                param.put("role","[0]");
                param.put("status",1);
                param.put("createTime",new Date());
                param.put("root",true);
                userDao.insert(param);
                Integer integer = userDao.searchIdByOpenId(openId);
                return  integer;
            }else {
                throw new EmosException("无法绑定超级管理员账号");
            }
            //普通员工的注册
        }else {

        }

        return 0;
    }

    @Override
    public Set<String> searchUserPermissions(int userId) {
        return userDao.searchUserPermissions(userId);
    }
}
