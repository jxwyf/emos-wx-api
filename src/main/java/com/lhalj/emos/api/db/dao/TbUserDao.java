package com.lhalj.emos.api.db.dao;

import com.lhalj.emos.api.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.Set;

@Mapper
public interface TbUserDao {

    //查询是否存在超级用户(root)
    boolean haveRootUser();

    //创建用户
    int insert(HashMap param);

    //查询用户主键值
    Integer searchIdByOpenId(String openId);

    //查询用户权限
    Set<String> searchUserPermissions(int userId);

    //查询用户信息
    TbUser searchById(int userId);

    //查询员工姓名和部门名称
    HashMap searchNameAndDept(int userId);



}