package com.hanger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author hanger
 * 2019-08-27 22:45
 */
@Configuration
public class MVCConfig implements WebMvcConfigurer {

//    private LoginInterceptor loginInterceptor;
//
//    @Autowired
//    public MVCConfig(LoginInterceptor loginInterceptor) {
//        this.loginInterceptor = loginInterceptor;
//    }

    //前后端跨域
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            /**
             * 设置头 使可以跨域访问
             * @param registry f
             * @since 4.2
             */
            @Override
            //重写父类提供的跨域请求处理的接口
            public void addCorsMappings(CorsRegistry registry) {
                //添加映射路径
                registry.addMapping("/**")
                        //放行哪些原始域
                        .allowedOrigins("*")
                        //是否发送Cookie信息
                        .allowCredentials(true)
                        //放行哪些原始域(请求方式)
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        //放行哪些原始域(头部信息)
                        .allowedHeaders("*")
                        //最大生命周期
                        .maxAge(3600)
                        //暴露哪些头部信息（因为跨域访问默认不能获取全部头部信息）
                        .exposedHeaders("token");
            }
        };
    }



    //重写添加拦截器方法并添加配置拦截器
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(loginInterceptor).addPathPatterns("/**")   //拦截哪些路径("/**":代表拦截所有路径);
//                .excludePathPatterns("/m_login", "/u_login"); //不拦截哪些路径;
//    }


}




