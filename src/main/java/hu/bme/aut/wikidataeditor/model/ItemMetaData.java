package hu.bme.aut.wikidataeditor.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemMetaData {
	String id;
	String url;
	String label;
	String description;
}
