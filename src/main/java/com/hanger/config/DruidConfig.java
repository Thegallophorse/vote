package com.hanger.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;


@Configuration
public class DruidConfig {
    //private static final Logger logger = LoggerFactory.getLogger(DruidConfig.class);

    //将所有前缀为spring.datasource下的配置项都加载到DataSource中
    //private static final String DB_PREFIX = "spring.datasource";

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource druidDataSource() {
        return new DruidDataSource();
    }

    @Bean
    public ServletRegistrationBean<StatViewServlet> druidStatViewServlet() {
        ServletRegistrationBean<StatViewServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new StatViewServlet());

        //访问路径
        srb.addUrlMappings("/druid/*");

        //IP白名单：
        srb.addInitParameter("allow","127.0.0.1");
        //IP黑名单(同时存在时，deny优先于allow)
        //servletRegistrationBean.addInitParameter("deny",IP);

        //druid控制台账号密码
        srb.addInitParameter("loginUsername","root");
        srb.addInitParameter("loginPassword","666666");

        //是否能够在控制台页面重置数据
        srb.addInitParameter("resetEnable","false");

        return srb;
    }



    /**
     * 配置服务过滤器
     *
     * @return 返回过滤器配置对象
     */
    @Bean
    public FilterRegistrationBean<WebStatFilter> druidWebStatFilter() {
        FilterRegistrationBean<WebStatFilter> frb = new FilterRegistrationBean<>();
        frb.setFilter(new WebStatFilter());

        // 添加过滤规则
        frb.addUrlPatterns("/*");
        // 忽略过滤格式
        frb.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");

        return frb;
    }



    //解决boot2.x下spring.datasource.filters=stat,wall,log4j无法正常配置的问题
//    @Component
//    @ConfigurationProperties(prefix = DB_PREFIX)
//    class DataSourceProperties {
//
//        private String filters;
//
//        //在同样的DataSource中，首先使用被Primary标注的DataSource
//        @Bean
//        @Primary
//        public DataSource dataSource() {
//            DruidDataSource datasource = new DruidDataSource();
//            try {
//                datasource.setFilters(filters);
//            } catch (SQLException e) {
//                logger.error("druid配置初始化filter出错: " + e);
//            }
//            return datasource;
//        }
//
//
////        public String getFilters() {
////            return filters;
////        }
////
////        public void setFilters(String filters) {
////            this.filters = filters;
////        }
//
//
//    }





}
















