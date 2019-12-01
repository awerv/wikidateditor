package hu.bme.aut.wikidataeditor.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties("wikidata")
public class WikidataProperties {
	private String url;
	private String sparql;
}