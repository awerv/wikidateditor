package hu.bme.aut.wikidataeditor.validator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.bme.aut.wikidataeditor.model.Entity;
import hu.bme.aut.wikidataeditor.model.PaintingDTO;
import hu.bme.aut.wikidataeditor.service.WikidataService;

@Service
public class PaintingValidator {
	
	@Autowired
	WikidataService wikidata;
	
	public List<String> validate(PaintingDTO painting) {
		
		List<String> errors = new ArrayList<>();
		
		if (painting.getId() != null) {
			checkItemValidity(painting.getId(), Entity.PAINTING, "painting", errors);
		}
		
		if (painting.getCreator() != null) {
			checkItemValidity(painting.getCreator(), Entity.HUMAN, "human", errors);
		}
		
		if (painting.getCtime() != null) {
			checkTimeValidity(painting.getCtime(), errors);
		}
		
		if (painting.getMaterials() != null && !painting.getMaterials().isEmpty()) {
			for (String material : painting.getMaterials()) {
				checkItemValidity(material, Entity.MATERIAL, "material", errors);
			}
		}
		
		if (painting.getLocation() != null) {
			checkItemValidity(painting.getLocation(), Entity.LOCATION, "location", errors);
		}
		
		return errors;
	}
	
	private void checkItemValidity(String entityId, String classId, String className, List<String> errors) {
		Boolean valid = wikidata.isEntityInClass(entityId, classId);
		if (valid == null) {
			errors.add(entityId + " does not exist");
		} else if (valid == Boolean.FALSE) {
			errors.add(entityId + " is not an instance of " + className);
		}
	}
	
	private void checkTimeValidity(LocalDate time, List<String> errors) {
		if(time.isAfter(LocalDate.now())) {
			errors.add("Creation time is in the future");
		}
	}
	
}
