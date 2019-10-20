package com.hanger.util;


import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Objects;


@Aspect
@Component

public class LogAspectUtil {
    private Logger logger = LoggerFactory.getLogger(LogAspectUtil.class);

    //全局变量
    private static final String LOG_HEAD = "🖥🔗🐘🔗🔍🔍🔍🔍🔍🔍🔗💬🗨🔗🔎🔎🔎🔎🔎🔎🔗🌐🔗👨‍";
    private static final String LOG_FOOT = "📃🔚👣👣👣👣👣👣👣👣👣👣👣👣👣👣👣👣👣👣👣👣👣👣";
    public static final String LOG_SQLEXE_EORROR = "      ___           ___           ___           ___           ___     \n" +
            "     /  /\\         /  /\\         /  /\\         /  /\\         /  /\\    \n" +
            "    /  /:/_       /  /::\\       /  /::\\       /  /::\\       /  /::\\   \n" +
            "   /  /:/ /\\     /  /:/\\:\\     /  /:/\\:\\     /  /:/\\:\\     /  /:/\\:\\  \n" +
            "  /  /:/ /:/_   /  /:/~/:/    /  /:/~/:/    /  /:/  \\:\\   /  /:/~/:/  \n" +
            " /__/:/ /:/ /\\ /__/:/ /:/___ /__/:/ /:/___ /__/:/ \\__\\:\\ /__/:/ /:/___\n" +
            " \\  \\:\\/:/ /:/ \\  \\:\\/:::::/ \\  \\:\\/:::::/ \\  \\:\\ /  /:/ \\  \\:\\/:::::/\n" +
            "  \\  \\::/ /:/   \\  \\::/~~~~   \\  \\::/~~~~   \\  \\:\\  /:/   \\  \\::/~~~~ \n" +
            "   \\  \\:\\/:/     \\  \\:\\        \\  \\:\\        \\  \\:\\/:/     \\  \\:\\     \n" +
            "    \\  \\::/       \\  \\:\\        \\  \\:\\        \\  \\::/       \\  \\:\\    \n" +
            "     \\__\\/         \\__\\/         \\__\\/         \\__\\/         \\__\\/  ";




    /**
     * 切点表达式:
     * 比如：execution(public * com.hanger.controller..*.*(..))
     * 意思是：类型为public的、返回值是任意类型的、
     * 在com.hanger.controller..包及其子包下的、
     * 任意类名下的、任意方法名的方法、
     * 并且形参个数是可变的。
     *
     * 说白了就是整个controller目录下的所有文件的所有方法
     */
    //这里是类型为public的、返回值是String的、在com.hanger.util下的、类名是LogUtil的、方法名是getLogHead的、形参为空的
    //Pointcut的切点函数log()必须为空！
    @Pointcut(value = "execution(public * com.hanger.controller..*.*(..))")
    public void log() {
    }

    @Before("log()")
    public void doBefore() {
        logger.info(LOG_HEAD);
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();

        //url
        logger.info("url=" + request.getRequestURI());

        //可以这样写输出时等号右边不带{}，{}代表第二个参数的返回值
//        logger.info("method={}", request.getMethod());
//
//        logger.info(request.getContentType());
//        logger.info(request.getProtocol());

        //ip&port&session
//        logger.info("请求者:" + request.getRemoteAddr() + ":" + request.getRemotePort());
//        logger.info("处理者:" + request.getLocalAddr() + ":" + request.getLocalPort());
        HttpSession session = request.getSession();
        logger.info("session:" + session.getAttribute("LOGIN_USER"));

        logger.info(LOG_FOOT);
    }

    @After("log()")
    public void doAfter() {
        //被标记的方法执行完后才执行，不是log()执行完后执行
    }




}
