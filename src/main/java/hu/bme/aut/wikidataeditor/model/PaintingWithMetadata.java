package hu.bme.aut.wikidataeditor.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaintingWithMetadata {
	private String id;
	private String url;
	private String label;
	private String description;
	private ItemMetaData creator;
	private String ctime;
	private String invnum;
	private List<ItemMetaData> materials;
	private ItemMetaData location;
}
