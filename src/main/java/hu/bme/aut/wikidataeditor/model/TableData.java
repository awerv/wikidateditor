package hu.bme.aut.wikidataeditor.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TableData {
	private List<PaintingListDTO> paintings;
	private String filter;
	private Integer count;
	private Integer page;
	private Integer pageSize;
	private Integer pageCount;
}