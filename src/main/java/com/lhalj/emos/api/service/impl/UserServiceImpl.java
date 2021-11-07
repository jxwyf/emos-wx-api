package com.lhalj.emos.api.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lhalj.emos.api.db.dao.TbDeptDao;
import com.lhalj.emos.api.db.dao.TbUserDao;
import com.lhalj.emos.api.db.pojo.MessageEntity;
import com.lhalj.emos.api.db.pojo.TbUser;
import com.lhalj.emos.api.exception.EmosException;
import com.lhalj.emos.api.service.UserService;
import com.lhalj.emos.api.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 描述:
 */
@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService {

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private TbDeptDao tbDeptDao;

    @Autowired
    private MessageTask messageTask;

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
                Integer id = userDao.searchIdByOpenId(openId);

                MessageEntity entity = new MessageEntity();
                entity.setSenderId(0);
                entity.setSenderName("系统信息");
                entity.setUuid(IdUtil.simpleUUID());
                entity.setMsg("欢迎你注册成为超级管理员 请及时更新你的员工个人信息");
                entity.setSendTime(new Date());
                messageTask.sendAsync(id+"",entity);

                return  id;
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

    @Override
    public Integer login(String code) {
        String openId = getOpenId(code);
        Integer id = userDao.searchIdByOpenId(openId);
        if (id==null) {
            throw new EmosException("账号不存在");
        }
        //TODO 从消息队列中接收
        messageTask.receiveAsync(id+"");
        return id;
    }

    @Override
    public TbUser searchById(int userId) {
        TbUser user = userDao.searchById(userId);
        return user;
    }

    @Override
    public String searchUserHiredate(int userId) {
        String hiredate = userDao.searchUserHiredate(userId);
        return hiredate;
    }

    @Override
    public HashMap searchUserSummary(int userId) {
        HashMap map = userDao.searchUserSummary(userId);
        return map;
    }



    @Override
    public ArrayList<HashMap> searchUserGroupByDept(String keyword) {
        //部门数据
        ArrayList<HashMap> list_1 = tbDeptDao.searchDeptMembers(keyword);
        //员工数据
        ArrayList<HashMap> list_2 = userDao.searchUserGroupByDept(keyword);

        for (HashMap map_1:list_1){
            long deptId = (long) map_1.get("id");
            ArrayList members = new ArrayList();
            for (HashMap map_2:list_2) {
                long id = (long) map_2.get("deptId");
                //员工id和部门id 相等 加入部门
                if(deptId == id){
                    members.add(map_2);
                }
            }
            map_1.put("members",members);
        }
        return list_1;
    }

    @Override
    public ArrayList<HashMap> searchMembers(List param) {
        ArrayList<HashMap> list = userDao.searchMembers(param);
        return list;
    }
}
