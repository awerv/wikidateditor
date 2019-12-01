package hu.bme.aut.wikidataeditor.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import hu.bme.aut.wikidataeditor.exception.UnauthorizedException;
import hu.bme.aut.wikidataeditor.model.PaintingDTO;
import hu.bme.aut.wikidataeditor.model.Credentials;
import hu.bme.aut.wikidataeditor.model.PaintingListDTO;
import hu.bme.aut.wikidataeditor.model.TableData;
import hu.bme.aut.wikidataeditor.property.WikidataProperties;
import hu.bme.aut.wikidataeditor.service.LoginService;
import hu.bme.aut.wikidataeditor.service.WikidataService;
import hu.bme.aut.wikidataeditor.validator.PaintingValidator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/")
public class RootController{
	
    @Autowired
	WikidataService wikidataService;
    
    @Autowired
    LoginService loginService;
    
    @Autowired
    WikidataProperties props;
    
    @Autowired
    PaintingValidator validator;
    
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody Credentials credentials, HttpServletRequest request) {
    	if (loginService.login(credentials, request)) {
    		return new ResponseEntity<>(HttpStatus.OK);
    	} else {
    		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    	}
    }

	@GetMapping("/login")
    public ResponseEntity<Void> login(HttpServletRequest request) {
    	if (loginService.isLoggedIn(request)) {
    		return new ResponseEntity<>(HttpStatus.OK);
    	} else {
    		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    	}
    }
    
    @GetMapping("/view/{id}")
	public String view(@PathVariable String id, Model model, HttpServletRequest request) {
		model.addAttribute("painting", wikidataService.getPaintingWithMetadata(id));
		return "view";
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
		
		List<PaintingListDTO> paintings = wikidataService.getPaintings(tableData);
		tableData.setPaintings(paintings);
		
		return new ResponseEntity<>(tableData, HttpStatus.OK);
	}
    
    @PostMapping("/create")
    public ResponseEntity<List<String>> create(@RequestBody PaintingDTO painting, HttpServletRequest request) {
    	checkLogin(request);
    	
    	List<String> errors = validator.validate(painting);
    	if (errors.isEmpty()) {
    		wikidataService.createPainting(painting, request);
    		return new ResponseEntity<>(null, HttpStatus.OK);
    	} else {
    		return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    	}
    }
    
    @PostMapping("/update")
    public ResponseEntity<List<String>> update(@RequestBody PaintingDTO painting, HttpServletRequest request) {
    	checkLogin(request);
    	
    	List<String> errors = validator.validate(painting);
    	if (errors.isEmpty()) {
    		wikidataService.updatePainting(painting,request);
    		return new ResponseEntity<>(null, HttpStatus.OK);
    	} else {
    		return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    	}
    }
    
    private void checkLogin(HttpServletRequest request) {
    	if (!loginService.isLoggedIn(request)) {
			throw new UnauthorizedException();
    	}
    }
}