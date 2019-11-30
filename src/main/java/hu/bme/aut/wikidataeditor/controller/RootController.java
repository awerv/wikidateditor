package hu.bme.aut.wikidataeditor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import hu.bme.aut.wikidataeditor.wrapper.SearchWrapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/")
public class RootController{
    @RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	public String search(@RequestBody String phrase){
		log.info(phrase);
		return SearchWrapper.search(phrase);
	}

}