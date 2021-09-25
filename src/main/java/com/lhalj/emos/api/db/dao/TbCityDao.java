package com.lhalj.emos.api.db.dao;

import com.lhalj.emos.api.db.pojo.TbCity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbCityDao {

    String searchCode(String city);
}