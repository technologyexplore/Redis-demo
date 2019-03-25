package com.demo.controller;

import java.util.concurrent.TimeUnit;

import com.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.demo.util.JedisUtil;

import javax.annotation.Resource;

@Controller
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 测试把数据设置到缓存
     *
     * @param name
     * @return
     */
    @RequestMapping("/test1")
    @ResponseBody
    public String test1(String name) {
        // 永久的key
        String foreverKey = "forever_key";
        // 有过期时间的key
        String effectiveKey = "effective_key";
        JedisUtil.set(foreverKey, "forever_value");
        JedisUtil.set(effectiveKey, "30 seconds", 30);
        for (int i = 0; i < 20; i++) {
            Object object = JedisUtil.get(foreverKey);
            Object object1 = JedisUtil.get(effectiveKey);
            Long expire = JedisUtil.getExpire(foreverKey);
            Long expire1 = JedisUtil.getExpire(effectiveKey);
            System.out.println(object + "--有时间：" + expire);
            System.out.println(object1 + "--有时间：" + expire1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "success";
    }

    /**
     * 验证码一分钟发送一次
     *
     * @param name
     * @return
     */
    @RequestMapping("/test2")
    @ResponseBody
    public String test2(String name) {
        String key = "15915839500";
        Object object = JedisUtil.get(key);
        if (object == null) {
            Long incr = JedisUtil.incr(key, 60, TimeUnit.SECONDS);
            return "验收码发送成功";
        } else {
            Long expire = JedisUtil.getExpire(key);
            return "一分钟内只能发送一次验证码，请在" + expire + "秒后再点击发送！";
        }
    }

    /**
     * 秒杀数量活动(计数器功能)
     *
     * @param name
     * @return
     */
    @RequestMapping("/test3")
    @ResponseBody
    public String test3(String name) {
        final String key = "iPhone X";
        JedisUtil.set(key, "9");
        for (int i = 0; i < 500; i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    Long decr = JedisUtil.decr(key);
                    if (decr < 0) {
                        // 告诉用户已经抢完了
                        System.out.println(Thread.currentThread().getName() + "：" + key + "已经被抢购完！");
                    } else {
                        //恭喜用户抢到了
                        System.out.println(Thread.currentThread().getName() + "：抢到了" + key);
                    }
                }
            }).start();
        }
        return "success";
    }

    /**
     * 分布式锁
     *
     * @param name
     * @return
     */
    @RequestMapping("/test4")
    @ResponseBody
    public String test4(String name) {
        final String key = "iPhone XX";
        final long expire = 5000;
        for (int i = 0; i < 3; i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < 6; i++) {
                        String threadName = Thread.currentThread().getName();
                        // 分布锁
                        boolean b = JedisUtil.retryLock(key, expire);
                        if (b) {
                            System.out.println(threadName + "：抢到了锁参与秒杀了！");
                            return;
                        }
                        try {
                            System.out.println(threadName + "：没抢到了锁，将进行下一次抢锁！");
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }).start();
        }
        return "success";
    }

    /**
     * 测试分布式事务
     *
     * @param name
     * @return
     */
    @RequestMapping("/test5")
    @ResponseBody
    public String test5(String name) {
        userService.testTransactional();
        return "ok";
    }

    /**
     * 测试大数据查询效率
     *
     * @param name
     * @return
     */
    @RequestMapping("/test6")
    @ResponseBody
    public String test6(String name) {
        userService.testBigData();
        return "ok";
    }

    /**
     * 测试新增排行榜
     *
     * @param name
     * @return
     */
    @RequestMapping("/test7")
    @ResponseBody
    public String test7(String name) {
        userService.testSort();
        return "ok";
    }

    /**
     * 测试查询排行榜
     *
     * @param name
     * @return
     */
    @RequestMapping("/test8")
    @ResponseBody
    public Object test8(String name) {
        return userService.getSort();
    }
}