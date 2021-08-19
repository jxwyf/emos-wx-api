package com.lhalj.emos.api.db.dao;

import com.lhalj.emos.api.db.pojo.TbRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbRoleDao {
    int deleteByPrimaryKey(Integer id);

    int insert(TbRole record);

    int insertSelective(TbRole record);

    TbRole selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TbRole record);

    int updateByPrimaryKey(TbRole record);
}