package hu.bme.aut.wikidataeditor.model;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaintingDTO {
	private String id;
	private String url;
	private String label;
	private String description;
	private String creator;
	private LocalDate ctime;
	private String invnum;
	private List<String> materials;
	private String location;
}
