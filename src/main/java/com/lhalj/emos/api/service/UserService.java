package com.lhalj.emos.api.service;

import java.util.Set;

public interface UserService {


    public int registerUser(String registerCode,String code,String nickname,String photo);

    Set<String> searchUserPermissions(int userId);
}
