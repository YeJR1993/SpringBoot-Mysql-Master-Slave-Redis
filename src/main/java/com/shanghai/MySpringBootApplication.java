package com.shanghai;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author YeJR
 * @EnableAsync 开启异步注解
 * @EnableRabbit 开启RabbitMQ支持
 * @EnableCaching 开启缓存支持
 * @EnableScheduling 开启定时任务
 * @ImportResource 导入xml文件
 * @ServletComponentScan 为了扫描在common文件夹下定义的filter
 * @EnableTransactionManagement 开启事物
 */
@EnableAsync
@EnableRabbit
@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
@ServletComponentScan(basePackages="com.shanghai.*")
@ImportResource(locations = { "classpath:druid-bean.xml" })
public class MySpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(MySpringBootApplication.class, args);
	}
}
