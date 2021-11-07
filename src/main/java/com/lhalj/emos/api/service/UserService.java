package com.lhalj.emos.api.service;

import com.lhalj.emos.api.db.pojo.TbUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface UserService {


    public int registerUser(String registerCode,String code,String nickname,String photo);

    Set<String> searchUserPermissions(int userId);

    Integer login(String code);

    TbUser searchById(int userId);

    String searchUserHiredate(int userId);

    HashMap searchUserSummary(int userId);

//    ArrayList<HashMap> searchDeptMembers(String keyword);


    ArrayList<HashMap> searchUserGroupByDept(String keyword);

    ArrayList<HashMap> searchMembers(List param);
}
