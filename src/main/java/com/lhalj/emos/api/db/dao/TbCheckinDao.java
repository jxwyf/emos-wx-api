package com.lhalj.emos.api.db.dao;

import com.lhalj.emos.api.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;

@Mapper
public interface TbCheckinDao {
   //查询今天是否签到过
   Integer haveCheckin(HashMap param);

   //保存签到
   void insert(TbCheckin entity);
}