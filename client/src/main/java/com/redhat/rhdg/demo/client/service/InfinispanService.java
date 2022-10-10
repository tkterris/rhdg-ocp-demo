package com.redhat.rhdg.demo.client.service;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("file:/var/identities.yaml")
public class InfinispanService {
	
	@Autowired
	Environment env;

	@Bean
	public RemoteCacheManager getCacheManager() {
		ConfigurationBuilder cb = new ConfigurationBuilder().addServer()
				.host("example-infinispan").port(11222)
				.security()
					.authentication()
						.username(env.getProperty("credentials.username"))
						.password(env.getProperty("credentials.password"))
				.clientIntelligence(ClientIntelligence.HASH_DISTRIBUTION_AWARE);
		System.out.println("Connecting with " + env.getProperty("credentials.username"));
		return new RemoteCacheManager(cb.build());
	}
}
