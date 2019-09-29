package com.polycis.tcpservice.scheduler;

import com.polycis.tcpservice.tcp.DiscardServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author : Wenyu Zhou
 * @version : v1.0
 * @date : 2019/4/18
 * description : 项目初始化后运行
 */
@Component
@Order(value = 1)
@Slf4j
public class StartService implements ApplicationRunner {
    @Resource
    private DiscardServer discardServer;

    @Override
    public void run(ApplicationArguments args) throws Exception{
        discardServer.run(8078);
    }
}
