package hu.bme.aut.wikidataeditor.data;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Painting {
	String name;
	String artist;
	LocalDate creation;
}
