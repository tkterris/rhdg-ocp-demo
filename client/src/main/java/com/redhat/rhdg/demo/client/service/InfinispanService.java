package com.redhat.rhdg.demo.client.service;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redhat.rhdg.demo.proto.DemoInitializer;
import com.redhat.rhdg.demo.proto.DemoInitializerImpl;

@Configuration
public class InfinispanService {

	@Value("${infinispan.host}")
	private String host;

	@Value("${infinispan.port}")
	private Integer port;

	@Value("${infinispan.username}")
	private String username;

	@Value("${infinispan.password}")
	private String password;

	@Value("${infinispan.cluster-aware}")
	private boolean clusterAware;

	private RemoteCacheManager rcm;

	@Bean
	public RemoteCacheManager getCacheManager() {
		// During application startup, create new RCM
		if (rcm == null) {
			ConfigurationBuilder builder = new ConfigurationBuilder();

			// RHDG cluster connection info
			builder.addServer().host(host).port(port);
			builder.security().authentication().username(username).password(password);
			if (!clusterAware) {
				builder.clientIntelligence(ClientIntelligence.BASIC);
			}

			// Register context initializer with Hot Rod client
			DemoInitializer initializer = new DemoInitializerImpl();
			builder.addContextInitializer(initializer);

			rcm = new RemoteCacheManager(builder.build());

			// Store protobuf files in the RHDG cluster to support querying (technically
			// not needed in this demo since the protofiles are in the server JAR,
			// but if no JAR is deployed then the client needs to register the files)
			rcm.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME)
					.put(initializer.getProtoFileName(), initializer.getProtoFile());
		}

		return rcm;
	}
}
