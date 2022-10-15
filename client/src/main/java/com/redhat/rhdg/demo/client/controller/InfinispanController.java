package com.redhat.rhdg.demo.client.controller;

import java.util.Map;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.redhat.rhdg.demo.model.DemoKey;
import com.redhat.rhdg.demo.model.DemoValue;

@RestController
@RequestMapping("/infinispan")
public class InfinispanController {

	@Value("${cache.name}")
	private String cache;
	
	@Autowired
	RemoteCacheManager rcm;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public Object[] getAll() {
		return rcm.getCache(cache).keySet().toArray();
	}
	
	@RequestMapping(value = "/{uid}", method = RequestMethod.GET)
	public Object get(@PathVariable(name = "uid") String uid) {
		return rcm.getCache(cache).get(new DemoKey(uid));
	}

	@RequestMapping(value = "/{uid}", method = RequestMethod.POST)
	public Object create(@PathVariable("uid") String uid, @RequestBody String value) {
		rcm.getCache(cache).put(new DemoKey(uid), new DemoValue(value));
		return value;
	}

	@RequestMapping(value = "/{uid}", method = RequestMethod.DELETE)
	public Object delete(@PathVariable("uid") String uid) {
		return rcm.getCache(cache).withFlags(Flag.FORCE_RETURN_VALUE).remove(new DemoKey(uid));
	}

	@RequestMapping(value = "/removeTask/{uid}", method = RequestMethod.POST)
	public Object executeTask(@PathVariable("uid") String uid) {
		return rcm.getCache(cache).execute("removeTask", Map.of("uid", uid));
	}
}
