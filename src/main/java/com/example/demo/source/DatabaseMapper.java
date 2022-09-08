package com.example.demo.source;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Mapper
public interface DatabaseMapper {
    @Select("${query}")
    List<LinkedHashMap<String, Object>> execute(String query);
}
