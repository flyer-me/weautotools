package com.flyerme.weautotools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * WeAutoTools 主应用启动类
 * 自动化工具平台后端服务
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-15
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
public class WeAutoToolsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeAutoToolsApplication.class, args);
	}

}
