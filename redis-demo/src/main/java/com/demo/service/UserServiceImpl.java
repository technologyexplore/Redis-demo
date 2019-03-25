package com.demo.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.entity.UserEntity;
import com.demo.mapper.UserMapper;
import com.demo.util.JedisUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SI-GZ-0953 on 2018/12/13.
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional
    public void testTransactional() {
        ///
        JedisUtil.set("transactional", "测试事务1");
        List<UserEntity> objects = new ArrayList<UserEntity>();
        objects.get(1).toString();
        ///
        JedisUtil.set("transactional", "测试事务2");
    }

    @Override
    public void testBigData() {
        long startTime = System.currentTimeMillis();
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper();
        List<UserEntity> userList = this.list(queryWrapper);
        long endTime = System.currentTimeMillis();
        long allTime = endTime - startTime;
        System.out.println("查询数据库耗时：" + allTime + "毫秒");

        startTime = System.currentTimeMillis();
        String userString = JedisUtil.get("userString").toString();
        endTime = System.currentTimeMillis();
        long allTime2 = endTime - startTime;
        System.out.println("查询Redis耗时：" + allTime2 + "毫秒");

        //Redis不支持直接将Java对象存储到数据库中
        //序列化再耗时也就几毫秒的事情，走业务逻如果不走redis，辑取结果的时间得是他几十上百倍，
        // 所以这点消耗可以忽略不计了。世上没有百分百完美的事情，只是权衡之后选更好的。
        BigDecimal bigDecimal = new BigDecimal(allTime);
        BigDecimal bigDecimal2 = new BigDecimal(allTime2);
        BigDecimal divide = bigDecimal.divide(bigDecimal2, 2, BigDecimal.ROUND_HALF_UP);
        System.out.println("查询Redis比查询数据库的耗时快了" + divide + "倍！");
        System.out.println();
        System.out.println();
        System.out.println();

    }

    @Override
    public void testSort() {
        UserEntity userEntity1 = new UserEntity();
        UserEntity userEntity2 = new UserEntity();
        UserEntity userEntity3 = new UserEntity();
        userEntity1.setId(111);
        userEntity1.setName("赖进杰1");
        userEntity1.setLikeNum(0D);
        userEntity2.setId(222);
        userEntity2.setName("赖进杰2");
        userEntity2.setLikeNum(0D);
        userEntity3.setId(333);
        userEntity3.setName("赖进杰3");
        userEntity3.setLikeNum(0D);
        JedisUtil.zAdd("id", JSON.toJSONString(userEntity1), userEntity1.getLikeNum());
        JedisUtil.zAdd("id", JSON.toJSONString(userEntity2), userEntity2.getLikeNum());
        JedisUtil.zAdd("id", JSON.toJSONString(userEntity3), userEntity3.getLikeNum());
        Object id = JedisUtil.hSet("id");
    }

    @Override
    public Object getSort() {
        UserEntity userEntity1 = new UserEntity();
        userEntity1.setId(333);
        userEntity1.setName("赖进杰3");
        userEntity1.setLikeNum(0D);
        JedisUtil.zAdd("id", JSON.toJSONString(userEntity1), 51D);
        Object object = JedisUtil.hSet("id");
        return object;
    }
}
