package hu.bme.aut.wikidataeditor.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import hu.bme.aut.wikidataeditor.auth.Credentials;
import hu.bme.aut.wikidataeditor.model.Paint;
import hu.bme.aut.wikidataeditor.model.TableData;
import hu.bme.aut.wikidataeditor.service.LoginService;
import hu.bme.aut.wikidataeditor.service.WikidataService;

@Controller
@RequestMapping("/")
public class RootController{
	
    @Autowired
	WikidataService wikidataService;
    
    @Autowired
    LoginService loginService;
    
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody Credentials credentials, HttpServletRequest request) {
    	if (loginService.login(credentials, request)) {
    		return new ResponseEntity<>(HttpStatus.OK);
    	} else {
    		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    	}
    }
	
	@GetMapping("/search")
	public ResponseEntity<TableData> getTableData(
			@RequestParam(name = "p", required = false, defaultValue = "0") int page,
			@RequestParam(name = "s", required = false, defaultValue = "15") int pageSize,
			@RequestParam(name = "f", required = false) String filter
		) {
		
		int itemCount = wikidataService.getNumberOfPaintings(filter);
		
		pageSize = Math.min(Math.max(pageSize, 0), 100);
		int maxPage = itemCount / pageSize;
		
		page = Math.min(Math.max(page, 0), maxPage);
		
		filter = filter == null ? null : filter.trim();
		
		TableData tableData = TableData.builder()
			.page(page).pageSize(pageSize)
			.count(itemCount).pageCount(maxPage + 1)
			.filter(filter).build();
		
		List<Paint> paintings = wikidataService.getPaintings(tableData);
		tableData.setPaintings(paintings);
		
		return new ResponseEntity<>(tableData, HttpStatus.OK);
	}
}