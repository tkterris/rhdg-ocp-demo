package com.redhat.rhdg.demo.proto;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
		basePackages = "com.redhat.rhdg.demo.model",
		schemaFileName = "model.proto",
		schemaFilePath = "proto/",
		schemaPackageName = "rhdg_demo")
public interface DemoInitializer extends GeneratedSchema {

}
