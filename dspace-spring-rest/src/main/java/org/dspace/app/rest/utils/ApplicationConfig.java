/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.utils;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.destination.BeanFactoryDestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.jms.support.destination.JndiDestinationResolver;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * This class provide extra configuration for our Spring Boot Application
 * 
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 *
 */
@EnableJms
@Configuration
@EnableSpringDataWebSupport
@SpringBootApplication
@ComponentScan({ "org.dspace.app.rest.converter", "org.dspace.app.rest.repository", "org.dspace.app.rest.utils", "org.dspace.log.appender" })
public class ApplicationConfig {


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

	@Bean
	public JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory,
													DefaultJmsListenerContainerFactoryConfigurer configurer) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConcurrency("1");

		// This provides all boot's default to this factory, including the message converter
		configurer.configure(factory, connectionFactory);
		factory.setConnectionFactory(connectionFactory);
		factory.setPubSubDomain(true);
		// You could still override some of Boot's default if necessary.
		return factory;
	}
}
