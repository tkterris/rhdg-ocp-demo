package com.redhat.rhdg.demo.client.service;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.marshall.JavaSerializationMarshaller;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redhat.rhdg.demo.model.ProtoInitializer;
import com.redhat.rhdg.demo.model.ProtoInitializerImpl;

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

	private RemoteCacheManager serialRcm;

	private RemoteCacheManager protoRcm;
	
	@Bean("serialRcm")
	public RemoteCacheManager getSerialCacheManager() {
		// During application startup, create new RCM
		if (serialRcm == null) {
			ConfigurationBuilder builder = getConfigurationBuilder();
			
			// Specify Java serialization context
			builder.marshaller(JavaSerializationMarshaller.class);
			builder.addJavaSerialAllowList("com.redhat.rhdg.demo.model.serial.*");

			serialRcm = new RemoteCacheManager(builder.build());
		}
		return serialRcm;
	}

	@Bean("protoRcm")
	public RemoteCacheManager getProtoCacheManager() {
		// During application startup, create new RCM
		if (protoRcm == null) {
			ConfigurationBuilder builder = getConfigurationBuilder();

			// Register Protobuf serialization context initializers
			ProtoInitializer initializer = new ProtoInitializerImpl();
			builder.addContextInitializer(initializer);

			protoRcm = new RemoteCacheManager(builder.build());

			// Store protobuf files in the RHDG cluster to support querying (technically
			// not needed in this demo since the protofiles are in the server JAR,
			// but if no JAR is deployed then the client needs to register the files)
			protoRcm.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME)
					.put(initializer.getProtoFileName(), initializer.getProtoFile());
		}
		return protoRcm;
	}

	private ConfigurationBuilder getConfigurationBuilder() {
		ConfigurationBuilder builder = new ConfigurationBuilder();

		// RHDG cluster connection info
		builder.addServer().host(host).port(port);
		builder.security().authentication().username(username).password(password);
		if (!clusterAware) {
			builder.clientIntelligence(ClientIntelligence.BASIC);
		}
		
		return builder;
	}
}
