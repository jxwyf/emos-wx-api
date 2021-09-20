package com.lhalj.emos.api.db.dao;

import com.lhalj.emos.api.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;

@Mapper
public interface TbCheckinDao {
   Integer haveCheckin(HashMap param);
}