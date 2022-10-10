package com.redhat.rhdg.demo.client.service;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfinispanService {

	@Bean
	public RemoteCacheManager getCacheManager() {
		ConfigurationBuilder cb = new ConfigurationBuilder().addServer()
				.host("example-infinispan").port(11222)
				.clientIntelligence(ClientIntelligence.BASIC);
		return new RemoteCacheManager(cb.build());
	}
}
