package com.redhat.rhdg.demo.server.loader;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.infinispan.persistence.spi.InitializationContext;
import org.infinispan.persistence.spi.MarshallableEntry;
import org.infinispan.persistence.spi.MarshallableEntryFactory;
import org.infinispan.persistence.spi.NonBlockingStore;
import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.SerializationContext;

import com.redhat.rhdg.demo.model.DemoValue;
import com.redhat.rhdg.demo.proto.DemoInitializer;
import com.redhat.rhdg.demo.proto.DemoInitializerImpl;

public class CustomStore<K, V> implements NonBlockingStore<K, V> {

	private Random random;
	private SerializationContext serializationCtx;
	private MarshallableEntryFactory<K, V> entryFactory;

	@Override
	public CompletionStage<Void> start(InitializationContext ctx) {
		return CompletableFuture.runAsync(() -> {
			this.random = new Random();
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
	public CompletionStage<MarshallableEntry<K, V>> load(int segment, Object key) {
		return CompletableFuture.supplyAsync(() -> {
			// generates a dummy "value"
			DemoValue value = new DemoValue(random.nextInt() + "");
			byte[] valueBytes = new byte[0];
			try {
				valueBytes = ProtobufUtil.toWrappedByteArray(serializationCtx, value);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return entryFactory.create(key, valueBytes);
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
