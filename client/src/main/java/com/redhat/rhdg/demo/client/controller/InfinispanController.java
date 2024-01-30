package com.redhat.rhdg.demo.client.controller;

import java.util.Map;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

	@GetMapping(value = "/")
	public Object[] query(@RequestParam(name = "query", required = true) String queryText) {
		// Execute a full-text query
		QueryFactory queryFactory = Search.getQueryFactory(rcm.getCache(cache));
		Query<DemoValue> query = queryFactory.create("FROM rhdg_demo.DemoValue WHERE data = :queryText");
		query.setParameter("queryText", queryText);
		return query.execute().list().toArray();
	}

	@GetMapping(value = "/{uid}")
	public Object get(@PathVariable(name = "uid") String uid) {
		return rcm.getCache(cache).get(new DemoKey(uid));
	}

	@PostMapping(value = "/{uid}")
	public Object create(@PathVariable("uid") String uid, @RequestBody String value) {
		rcm.getCache(cache).put(new DemoKey(uid), new DemoValue(value));
		return value;
	}

	@DeleteMapping(value = "/{uid}")
	public Object delete(@PathVariable("uid") String uid) {
		return rcm.getCache(cache).withFlags(Flag.FORCE_RETURN_VALUE).remove(new DemoKey(uid));
	}

	@PostMapping(value = "/removeTask/{uid}")
	public Object executeTask(@PathVariable("uid") String uid) {
		return rcm.getCache(cache).execute("removeTask", Map.of("uid", uid));
	}
}
