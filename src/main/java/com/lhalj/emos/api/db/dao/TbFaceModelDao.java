package com.lhalj.emos.api.db.dao;

import com.lhalj.emos.api.db.pojo.TbFaceModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbFaceModelDao {

    //查询人脸数据
    String searchFaceModel(int userId);

    //插入人脸数据
    void insert(TbFaceModel faceModel);

    //删除人脸数据
    int deleteFaceModel(int userId);
}