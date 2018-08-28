/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.configuration;

import javax.annotation.Resource;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

/**
 * Class to configure the embedded Active MQ server
 */
@Configuration
public class ActiveMqConfiguration {

    @Resource(name="jndiProperties")
    private java.util.Properties properties;

    @Bean
    public PropertiesFactoryBean jndiProperties() {
        PropertiesFactoryBean pfb = new PropertiesFactoryBean();
        pfb.setLocation(new ClassPathResource("jndi.properties"));
        return pfb;
    }

    @Bean
    public JndiTemplate jndiTemplate() {
        JndiTemplate jndiTemplate = new JndiTemplate();
        jndiTemplate.setEnvironment(properties);
        return jndiTemplate;
    }

    @Bean(name = "jmsConnectionFactory")
    public JndiObjectFactoryBean jmsConnectionFactory() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiTemplate(jndiTemplate());
        jndiObjectFactoryBean.setJndiName("JmsConnectionFactory");

        return jndiObjectFactoryBean;
    }

}
