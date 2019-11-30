package hu.bme.aut.wikidataeditor.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Paint {
	private String label;
	private String id;
	private String url;
	private String description;
}