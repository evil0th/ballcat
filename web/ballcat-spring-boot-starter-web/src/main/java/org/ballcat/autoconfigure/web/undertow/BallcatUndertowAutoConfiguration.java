/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballcat.autoconfigure.web.undertow;

import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.spec.ServletContextImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import java.io.File;

/**
 * @author lingting 2023-06-12 16:07
 */
@AutoConfiguration
@ConditionalOnClass(DeploymentInfo.class)
public class BallcatUndertowAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public BallcatUndertowServletWebServerFactory undertowServletWebServerFactoryCustomization() {
		return new BallcatUndertowServletWebServerFactory();
	}

	/**
	 * 定时创建 undertow 的用来上传文件的临时文件夹, 避免文件上传异常 /tmp/undertow.35301.2529636817692511076
	 */
	@Bean
	@ConditionalOnMissingBean
	public BallcatUndertowTimer undertowTimer(ServletContext context) {
		File dir = null;
		if (context instanceof ServletContextImpl) {
			Deployment deployment = ((ServletContextImpl) context).getDeployment();
			DeploymentInfo deploymentInfo = deployment.getDeploymentInfo();
			MultipartConfigElement config = deploymentInfo.getDefaultMultipartConfig();
			if (config != null && StringUtils.hasText(config.getLocation())) {
				dir = new File(config.getLocation());
			}
			else {
				dir = deploymentInfo.getTempDir();
			}
		}

		return new BallcatUndertowTimer(dir);
	}

}
