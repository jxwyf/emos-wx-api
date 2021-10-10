package com.lhalj.emos.api.db.dao;

import com.lhalj.emos.api.db.pojo.TbHolidays;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbHolidaysDao {


    Integer searchTodayIsHolidays();

    //查询是否是特殊节假日
    ArrayList<String> searchHolidaysInRange(HashMap param);

}