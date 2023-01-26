package com.redhat.rhdg.demo.client.service;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.MarshallerUtil;
import org.infinispan.protostream.SerializationContext;
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

	@Bean
	public RemoteCacheManager getCacheManager() {
		DemoInitializer initializer = new DemoInitializerImpl();
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.addServer().host(host).port(port);
		builder.security().authentication()
					.username(username)
					.password(password);
		if (!clusterAware) {
			builder.clientIntelligence(ClientIntelligence.BASIC);
		}
		RemoteCacheManager rcm = new RemoteCacheManager(builder.build());
		SerializationContext context = MarshallerUtil.getSerializationContext(rcm);
		initializer.registerSchema(context);
		initializer.registerMarshallers(context);
		
		// The following section is usually needed to provide the protofiles to the RHDG
		// cluster, but in this case the protofiles are included with the server JAR
		// and so this step is not required.
		/*
		RemoteCache<String, String> protoMetadataCache = rcm
				.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
		protoMetadataCache.put(initializer.getProtoFileName(), initializer.getProtoFile());
		String errors = protoMetadataCache.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
		if (errors != null) {
			throw new IllegalStateException("Some Protobuf schema files contain errors: " + errors + "\nSchema :\n"
					+ initializer.getProtoFileName());
		}
		*/
		
		return rcm;
	}
}
