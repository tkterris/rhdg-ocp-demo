package com.redhat.rhdg.demo.server.loader;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.infinispan.commons.configuration.ConfiguredBy;
import org.infinispan.commons.marshall.WrappedByteArray;
import org.infinispan.persistence.spi.InitializationContext;
import org.infinispan.persistence.spi.MarshallableEntry;
import org.infinispan.persistence.spi.MarshallableEntryFactory;
import org.infinispan.persistence.spi.NonBlockingStore;
import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.SerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.rhdg.demo.model.DemoKey;
import com.redhat.rhdg.demo.model.DemoValue;
import com.redhat.rhdg.demo.proto.DemoInitializer;
import com.redhat.rhdg.demo.proto.DemoInitializerImpl;

@ConfiguredBy(CustomStoreConfiguration.class)
public class CustomStore<K, V> implements NonBlockingStore<K, V> {

	private SerializationContext serializationCtx;
	private MarshallableEntryFactory<K, V> entryFactory;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public CompletionStage<Void> start(InitializationContext ctx) {
		return CompletableFuture.runAsync(() -> {
			this.serializationCtx = ProtobufUtil.newSerializationContext();
			DemoInitializer initializer = new DemoInitializerImpl();
			initializer.registerSchema(this.serializationCtx);
			initializer.registerMarshallers(this.serializationCtx);
			
			this.entryFactory = ctx.getMarshallableEntryFactory();
			return;
		});
	}

	@Override
	public CompletionStage<Void> stop() {
		return CompletableFuture.runAsync(() -> {});
	}

	@Override
	public CompletionStage<MarshallableEntry<K, V>> load(int segment, Object keyBytes) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				logger.info("Loading value for key " + keyBytes.toString());
				DemoKey key = ProtobufUtil.fromWrappedByteArray(serializationCtx, 
						((WrappedByteArray) keyBytes).getBytes());
				// generates a dummy value from the key, by just taking the key's hash
				DemoValue value = new DemoValue(key.hashCode() + "");
				byte[] valueBytes = ProtobufUtil.toWrappedByteArray(serializationCtx, value);
				return entryFactory.create(keyBytes, valueBytes);
			} catch (Exception e) {
				logger.error("Marshalling failed for key " + keyBytes.toString(), e);
				return entryFactory.create(keyBytes, new byte[0]);
			}
		});
	}

	@Override
	public CompletionStage<Void> write(int segment, MarshallableEntry<? extends K, ? extends V> entry) {
		return CompletableFuture.runAsync(() -> {});
	}

	@Override
	public CompletionStage<Boolean> delete(int segment, Object key) {
		return CompletableFuture.supplyAsync(() -> false);
	}

	@Override
	public CompletionStage<Void> clear() {
		return CompletableFuture.runAsync(() -> {});
	}

}
