package hu.bme.aut.wikidataeditor.service;

import org.springframework.stereotype.Service;

import hu.bme.aut.wikidataeditor.model.Paint;
import hu.bme.aut.wikidataeditor.model.TableData;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

@Service
public class WikidataService {
	
	public Integer getNumberOfPaintings(String filter) {
		String filterString = " SERVICE wikibase:label {"
				+ "  bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\"."
				+ "} ";
		if (filter != null && !filter.isEmpty()) {
			filterString = 
				"  ?item rdfs:label ?itemLabel. "
				+ "  FILTER(LANG(?itemLabel) = \"en\") "
				+ "FILTER (CONTAINS(?itemLabel, \"" + filter + "\")) . ";
		}
		
		String queryString = 
				"SELECT (COUNT(?item) AS ?count) "
				+ "WHERE {"
				+ "  ?item wdt:P31 wd:Q3305213. "
				+ filterString
				+ "}";
		
		SPARQLRepository sparqlRepository = new SPARQLRepository("https://query.wikidata.org/sparql");
		try (RepositoryConnection sparqlConnection = sparqlRepository.getConnection()) {
			
			TupleQuery tupleQuery = sparqlConnection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			
			return Integer.parseInt(result.next().getBinding("count").getValue().stringValue());
		}
	}
	
	public List<Paint> getPaintings(TableData tableData) {
		List<Paint> paintings = new ArrayList<>();
		
		String filter = tableData.getFilter();
		String filterString = " SERVICE wikibase:label {"
				+ "  bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\"."
				+ "} ";
		if (filter != null && !filter.isEmpty()) {
			filterString = 
				"  ?item schema:description ?itemDescription. "
				+ "  ?item rdfs:label ?itemLabel. "
				+ "  FILTER(LANG(?itemLabel) = \"en\") "
				+ "  FILTER(LANG(?itemDescription) = \"en\") "
				+ "FILTER (CONTAINS(?itemLabel, \"" + filter + "\")) . ";
		}
		
		String queryString = 
				"SELECT ?item ?itemDescription ?itemLabel "
				+ "WHERE {"
				+ "  ?item wdt:P31 wd:Q3305213. "
				+ filterString
				+ "} "
				+ "OFFSET " + (tableData.getPageSize() * tableData.getPage()) + " "
				+ "LIMIT " + tableData.getPageSize();
		
		
		SPARQLRepository sparqlRepository = new SPARQLRepository("https://query.wikidata.org/sparql");
		try (RepositoryConnection sparqlConnection = sparqlRepository.getConnection()) {
			
			TupleQuery tupleQuery = sparqlConnection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			
			while (result.hasNext()) {
				BindingSet bs = result.next();
				
				Binding b = bs.getBinding("item");
				String url = b == null ? null : b.getValue().toString();
				
				b = bs.getBinding("itemDescription");
				String description = b == null ? null : b.getValue().toString();
				
				b = bs.getBinding("itemLabel");
				String label = b == null ? null : b.getValue().toString();
				
				String[] tokens = url.split("/");
				String id = tokens.length > 0 ? tokens[tokens.length - 1] : null;
				
				Paint painting = Paint.builder()
					.id(id).url(url)
					.label(withoutLanguageCode(label))
					.description(withoutLanguageCode(description))
					.build();
				paintings.add(painting);
			}
			
			return paintings;
		}
	}
	
	public String withoutLanguageCode(String raw) {
		if(raw !=null && raw.startsWith("\"") && raw.endsWith("\"@en")) {
			return raw.substring(1, raw.length() - 4);
		}
		
		return raw;
	}
}
