package yin.xuebiblockchain.Config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import yin.xuebiblockchain.Utils.LoginInterceptor;

/**
 * MVC 配置类
 *
 * 【改造说明】
 * 1. 【Bug修复】LoginInterceptor 改为 Spring 注入，不再 new 创建
 *    原来写的是 new LoginInterceptor()，这样 Spring 不会注入 loginMapper，
 *    导致 LoginInterceptor 中的 loginMapper 为 null，一查数据库就报空指针。
 *
 * 2. 新增 /resource/list、/resource/hot 等公开接口的排除路径
 *    这些接口不需要登录就能访问（首页展示用）。
 *
 * 3. 需要登录的接口（如 /resource/publish、/resource/my）不在排除列表中，
 *    会经过拦截器验证 token。
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    /**
     * 【Bug修复】通过 Spring 注入 LoginInterceptor
     * 这样 LoginInterceptor 中的 @Resource loginMapper 才能正常工作
     */
    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        // ===== 登录相关（不需要认证）=====
                        "/user/login",
                        "/user/sendCode",
                        "/user/loginByPassword",

                        // ===== 资源公开接口（不需要登录就能浏览）=====
                        "/resource/list",
                        "/resource/hot",
                        "/resource/category",
                        "/resource/search",
                        "/resource/detail/**",
                        "/resource/trace/**",

                        // ===== 新闻和帖子公开接口 =====
                        "/news",
                        "/news/**",
                        "/post",
                        "/post/getUserName",
                        "/post/getPostData",

                        // ===== 用户信息公开接口 =====

                        "/userInfo/userAddress",
                        "/userInfo/userFans",
                        "/userInfo/userFollow",
                        "/userInfo/userVIP"
                )
                .order(1);

        System.out.println("【拦截器已配置】公开接口已放行，需要登录的接口已保护");
    }
}