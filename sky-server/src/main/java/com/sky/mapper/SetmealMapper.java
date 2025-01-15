package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.Autofill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.*;

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

    //select s.*, d.name as dish_name from setmeal s left join setmeal_dish d on s.id=d.setmeal_id
    //WHERE s.name LIKE 'hello%' AND s.category_id = ? and s.status=1;
    Page<SetmealVO> queryPage(SetmealPageQueryDTO setmealPageQueryDTO);

    @Select("select * from setmeal where id=#{id}")
    Setmeal getById(Long id);

    @Autofill(value=OperationType.UPDATE)
    void update(Setmeal setmeal);


    void deleteByIds(Long[] ids);
}
