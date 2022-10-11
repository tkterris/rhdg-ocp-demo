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
	
	@RequestMapping(value = "/{key}", method = RequestMethod.GET)
	public Object get(@PathVariable(name = "key") String key) {
		return rcm.getCache(cache).get(key);
	}

	@RequestMapping(value = "/{key}", method = RequestMethod.POST)
	public Object create(@PathVariable("key") String key, @RequestBody String value) {
		return rcm.getCache(cache).put(key, value);
	}

	@RequestMapping(value = "/{key}", method = RequestMethod.DELETE)
	public Object delete(@PathVariable("key") String key) {
		return rcm.getCache(cache).withFlags(Flag.FORCE_RETURN_VALUE).remove(key);
	}

	@RequestMapping(value = "/removeTask/{key}", method = RequestMethod.POST)
	public Object executeTask(@PathVariable("key") String key) {
		return rcm.getCache().execute("removeTask", Map.of("key", key));
	}
}
