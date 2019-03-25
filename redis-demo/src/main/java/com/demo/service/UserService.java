package com.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.entity.UserEntity;
import org.springframework.stereotype.Service;

/**
 * Created by SI-GZ-0953 on 2018/12/13.
 */
public interface UserService  extends IService<UserEntity> {
    void testTransactional();
    void testBigData();
    void testSort();
    Object getSort();
}
