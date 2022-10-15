package com.redhat.rhdg.demo.model;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

public class DemoValue {

	@ProtoField(1)
	String data;
	
	@ProtoFactory
	public DemoValue(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "DemoValue [data=" + data + "]";
	}
}
