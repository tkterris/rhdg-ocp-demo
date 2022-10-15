package com.redhat.rhdg.demo.client.service;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.MarshallerUtil;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.redhat.rhdg.demo.proto.DemoSchemaGenerator;
import com.redhat.rhdg.demo.proto.DemoSchemaGeneratorImpl;

@org.springframework.context.annotation.Configuration
public class InfinispanService {

	@Value("${infinispan.username}")
	private String username;

	@Value("${infinispan.password}")
	private String password;

	@Bean
	public RemoteCacheManager getCacheManager() {
		DemoSchemaGenerator initializer = new DemoSchemaGeneratorImpl();
		Configuration configuration = new ConfigurationBuilder().addServer().host("example-infinispan").port(11222)
				.security().authentication().username(username).password(password)
				.build();
		RemoteCacheManager rcm = new RemoteCacheManager(configuration);
		SerializationContext context = MarshallerUtil.getSerializationContext(rcm);
		initializer.registerSchema(context);
		initializer.registerMarshallers(context);
		RemoteCache<String, String> protoMetadataCache = rcm
				.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
		protoMetadataCache.put(initializer.getProtoFileName(), initializer.getProtoFile());
		String errors = protoMetadataCache.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
		if (errors != null) {
			throw new IllegalStateException("Some Protobuf schema files contain errors: " + errors + "\nSchema :\n"
					+ initializer.getProtoFileName());
		}
		return rcm;
	}
}
