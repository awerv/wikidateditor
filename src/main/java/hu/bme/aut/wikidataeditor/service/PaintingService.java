package hu.bme.aut.wikidataeditor.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import hu.bme.aut.wikidataeditor.model.Painting;

@Service
public class PaintingService {
	private List<Painting> paintings = new ArrayList<>();
	
	@PostConstruct
	public void init() {
		paintings = Arrays.asList(
			Painting.builder().name("Rough Sea at Belle-Ile").artist("ŒLEWIÑSKI, W³adis³aw").creation(LocalDate.of(1994, 01, 01)).build(),
			Painting.builder().name("Allegory").artist("AACHEN, Hans von").creation(LocalDate.of(1598, 01, 01)).build(),
			Painting.builder().name("Ceramic Floor").artist("ABAQUESNE, Masséot").creation(LocalDate.of(1542, 04, 20)).build(),
			Painting.builder().name("Cloister").artist("ABBATI, Giuseppe").creation(LocalDate.of(1861, 11, 21)).build(),
			Painting.builder().name("Music Room").artist("ADAM, Robert").creation(LocalDate.of(1773, 02, 10)).build(),
			Painting.builder().name("Exterior view").artist("ADELCRANTZ, Carl Fredrik").creation(LocalDate.of(1766, 01, 10)).build(),
			Painting.builder().name("Episodes in Roman History").artist("ADEMOLLO, Luigi").creation(LocalDate.of(1820, 12, 10)).build(),
			Painting.builder().name("St Mary Magdalene").artist("ALGARDI, Alessandro").creation(LocalDate.of(1640, 10, 11)).build(),
			Painting.builder().name("Frontispiece").artist("AMEDEI, Giuliano").creation(LocalDate.of(1465, 07, 18)).build(),
			Painting.builder().name("Venus").artist("AMENDOLA, Giovanni Battista").creation(LocalDate.of(1310, 01, 12)).build(),
			Painting.builder().name("Famous Persons: Queen Esther").artist("ANDREA DEL CASTAGNO").creation(LocalDate.of(1777, 11, 04)).build(),
			Painting.builder().name("Last Supper").artist("Andrea Del Castagno").creation(LocalDate.of(1447, 8, 02)).build()
		);
	}
	
	public List<Painting> findAll() {
		return paintings;
	}
	
	public Painting findById(Integer id) {
		return paintings.get(id);
	}
	
	public Integer save(Integer id, Painting painting) {
		if (id == null) {
			paintings.add(painting);
			return paintings.size()-1;
		} else {
			paintings.add(id, painting);
			return id;
		}
	}
	
	public void delete(Integer id) {
		paintings.remove(id.intValue());
	}
}
