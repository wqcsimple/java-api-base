package com.whis.app.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Component

@Mapper
@Component
interface AdminMapper {

    @Select("""
        select * from `user` where id = #{id}
    """)
    fun finById(@Param("id") id: Long): Map<String, Any>?
}