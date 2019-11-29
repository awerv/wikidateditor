package hu.bme.aut.wikidataeditor.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import hu.bme.aut.wikidataeditor.data.Painting;
import hu.bme.aut.wikidataeditor.service.PaintingService;

@Controller
@RequestMapping("/site")
public class SiteController {
	
	@Autowired
	PaintingService paintingService;
	
	@GetMapping("/*")
	public String index(Model model, HttpServletRequest request) {
		model.addAttribute("paintings", paintingService.findAll());
		return "index";
	}
	
	@GetMapping("/view/{id}")
	public String view(@PathVariable Integer id, Model model, HttpServletRequest request) {
		model.addAttribute("painting", paintingService.findById(id));
		return "view";
	}
	
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Integer id, Model model, HttpServletRequest request) {
		model.addAttribute("painting", paintingService.findById(id));
		model.addAttribute("mode", "EDIT");
		return "edit";
	}
	
	@GetMapping("/create")
	public String create(Model model, HttpServletRequest request) {
		model.addAttribute("paintings", paintingService.findAll());
		model.addAttribute("mode", "CREATE");
		return "edit";
	}
	
	@PostMapping(value = {"/save", "/save/{id}"})
	public String save(@PathVariable Integer id, @RequestBody Painting painting, Model model, HttpServletRequest request) {
		Integer savedId = paintingService.save(id, painting);
		return "redirect:/site/view/" + savedId;
	}
	
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Integer id, HttpServletRequest request) {
		paintingService.delete(id);
		return "redirect:/site";
	}
}
