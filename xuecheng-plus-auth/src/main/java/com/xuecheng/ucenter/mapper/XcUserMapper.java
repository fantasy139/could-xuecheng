package com.xuecheng.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.ucenter.model.po.XcUser;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface XcUserMapper extends BaseMapper<XcUser> {

    /**
     * 根据用户id查询出用户的权限
     * @param id
     * @return {@code List<String> }
     * @author fantasy
     * @date 2024-03-16
     * @since version
     */
    @Select("SELECT code FROM xc_menu WHERE id IN (SELECT menu_id FROM xc_permission WHERE role_id IN ( SELECT role_id FROM xc_user_role WHERE user_id = #{userId} ))")
    List<String> selectPermissionList(String id);
}
