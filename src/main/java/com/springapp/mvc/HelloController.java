package com.springapp.mvc;

import com.alibaba.fastjson.JSON;
import com.springapp.model.News;
import com.springapp.service.JestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/hello")
public class HelloController {

	@Autowired
	private JestService searchNewsService;

	@RequestMapping(method = RequestMethod.GET)
	public String printWelcome(ModelMap model) {
		model.addAttribute("message", "Hello world!");
		
		return "hello";
	}

	@RequestMapping(method = RequestMethod.GET,value = "/createIndex")
	public @ResponseBody void createIndex(){
		searchNewsService.builderSearchIndex();
	}

	@RequestMapping(method = RequestMethod.GET,value = "/search",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public Object search(@RequestParam("params")String params){
		List<News> o = searchNewsService.searchNews(params);
		return JSON.toJSONString(o);
	}
}