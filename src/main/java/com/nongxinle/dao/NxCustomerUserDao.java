package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 04-14 17:42
 */

import com.nongxinle.entity.NxCustomerUserEntity;

import java.util.Map;


public interface NxCustomerUserDao extends BaseDao<NxCustomerUserEntity> {



    NxCustomerUserEntity queryUserByOpenId(String openid);

    Map<String, Object> queryCustomerUserInfo(Integer gbDepartmentUserId);
}
