package com.sky.mapper;

import com.sky.annotation.Autofill;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO setmeal ("
            + " category_id, name, price, status, description, image, "
            + " update_time, update_user, create_time, create_user"
            + ") VALUES ("
            + " #{categoryId}, #{name}, #{price}, #{status}, #{description}, #{image}, "
            + " #{updateTime}, #{updateUser}, #{createTime}, #{createUser}"
            + ")")
    @Autofill(value= OperationType.INSERT)
    void save(Setmeal setmeal);

}
