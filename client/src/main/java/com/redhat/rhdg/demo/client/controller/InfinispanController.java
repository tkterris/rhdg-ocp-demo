package com.redhat.rhdg.demo.client.controller;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/infinispan")
public class InfinispanController {
	
	private static final String CACHE = "test-cache";
	
	@Autowired
	RemoteCacheManager rcm;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public Object[] getAll() {
		return rcm.getCache(CACHE).keySet().toArray();
	}
	
	@RequestMapping(value = "/{key}", method = RequestMethod.GET)
	public Object get(@PathVariable(name = "key") String key) {
		return rcm.getCache(CACHE).get(key);
	}

	@RequestMapping(value = "/{key}", method = RequestMethod.POST)
	public Object create(@PathVariable("key") String key, @RequestBody String value) {
		return rcm.getCache(CACHE).put(key, value);
	}

	@RequestMapping(value = "/{key}", method = RequestMethod.DELETE)
	public Object delete(@PathVariable("key") String key) {
		return rcm.getCache(CACHE).withFlags(Flag.FORCE_RETURN_VALUE).remove(key);
	}
}
