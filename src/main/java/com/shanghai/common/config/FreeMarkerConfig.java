package com.shanghai.common.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.shanghai.common.tag.MenuTag;

/**
* @author: YeJR 
* @version: 2018年5月31日 上午11:41:28
* freemarker配置
*/
@Configuration
public class FreeMarkerConfig {
	
	@Autowired
    protected freemarker.template.Configuration configuration;
	
	@Autowired
    protected MenuTag menuTag;
	
	/**
	 * 自定义标签
	 */
	@PostConstruct
    public void setSharedVariable() {
        // menu即为页面上调用的标签名
        configuration.setSharedVariable("menu", menuTag);
    }

}
