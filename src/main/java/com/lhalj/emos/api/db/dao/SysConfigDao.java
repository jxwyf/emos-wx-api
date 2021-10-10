package com.lhalj.emos.api.db.dao;

import com.lhalj.emos.api.db.pojo.SysConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysConfigDao {
    List<SysConfig> selectAllParam();
}