package com.redhat.rhdg.demo.model;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

public class DemoKey {

	@ProtoField(1)
	String uid;
	
	@ProtoFactory
	public DemoKey(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	@Override
	public String toString() {
		return "DemoKey [uid=" + uid + "]";
	}
}
