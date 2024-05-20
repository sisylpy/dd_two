package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 05-15 08:30
 */

import com.nongxinle.entity.NxCommunityCouponEntity;
import com.nongxinle.entity.NxCustomerUserCouponEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerUserCouponService {


    void save(NxCustomerUserCouponEntity customerUserCouponEntity);


    List<NxCustomerUserCouponEntity> queryUserCouponListByParams(Map<String, Object> map);

    NxCustomerUserCouponEntity equalObject(Integer nxCosCucId);

    void update(NxCustomerUserCouponEntity userCouponEntity);

    NxCustomerUserCouponEntity queryUserCouponDetail(Integer id);
}
