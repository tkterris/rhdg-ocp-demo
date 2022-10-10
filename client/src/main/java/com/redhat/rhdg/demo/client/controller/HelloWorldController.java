package com.redhat.rhdg.demo.client.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String hello() {
		return "Hello NgBilling";
	}
}
