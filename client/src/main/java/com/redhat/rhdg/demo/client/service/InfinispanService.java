package com.redhat.rhdg.demo.client.service;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfinispanService {
	
	@Value("${infinispan.username}")
	private String username;
	
	@Value("${infinispan.password}")
	private String password;

	@Bean
	public RemoteCacheManager getCacheManager() {
		ConfigurationBuilder cb = new ConfigurationBuilder().addServer()
				.host("example-infinispan").port(11222)
				.security()
					.authentication()
						.username("developer")
						.password(password)
				.clientIntelligence(ClientIntelligence.HASH_DISTRIBUTION_AWARE);
		return new RemoteCacheManager(cb.build());
	}
}
