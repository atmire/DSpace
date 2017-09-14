/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.utils;

import org.dspace.app.rest.parameter.resolver.SearchFilterResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * This class provide extra configuration for our Spring Boot Application
 * 
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 *
 */
@Configuration
@EnableSpringDataWebSupport
@ComponentScan({ "org.dspace.app.rest.converter", "org.dspace.app.rest.repository", "org.dspace.app.rest.utils" })
public class ApplicationConfig extends WebMvcConfigurerAdapter {
	@Value("${dspace.dir}")
	private String dspaceHome;

	@Value("${cors.allowed-origins}")
	private String corsAllowedOrigins;

	public String getDspaceHome() {
		return dspaceHome;
	}

	public String[] getCorsAllowedOrigins() {
		if (corsAllowedOrigins != null)
			return corsAllowedOrigins.split("\\s*,\\s*");
		return null;
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new SearchFilterResolver());
	}

}
