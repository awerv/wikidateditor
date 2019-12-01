package hu.bme.aut.wikidataeditor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder;
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder;
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl;
import org.wikidata.wdtk.datamodel.implementation.MonolingualTextValueImpl;
import org.wikidata.wdtk.datamodel.implementation.PropertyIdValueImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.wikibaseapi.ApiConnection;
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataEditor;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import hu.bme.aut.wikidataeditor.model.ItemMetaData;
import hu.bme.aut.wikidataeditor.model.PaintingDTO;
import hu.bme.aut.wikidataeditor.model.PaintingListDTO;
import hu.bme.aut.wikidataeditor.model.PaintingWithMetadata;
import hu.bme.aut.wikidataeditor.model.TableData;
import hu.bme.aut.wikidataeditor.property.WikidataProperties;

import static hu.bme.aut.wikidataeditor.model.Property.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

@Service
public class WikidataService {
	
	@Autowired
	WikidataProperties wikidataProperties;
	
	@Autowired
	LoginService loginService;
	
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
		
		SPARQLRepository sparqlRepository = new SPARQLRepository(wikidataProperties.getSparql());
		try (RepositoryConnection sparqlConnection = sparqlRepository.getConnection()) {
			
			TupleQuery tupleQuery = sparqlConnection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult result = tupleQuery.evaluate();
			
			return Integer.parseInt(result.next().getBinding("count").getValue().stringValue());
		}
	}
	
	public List<PaintingListDTO> getPaintings(TableData tableData) {
		List<PaintingListDTO> paintings = new ArrayList<>();
		
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
		
		
		SPARQLRepository sparqlRepository = new SPARQLRepository(wikidataProperties.getSparql());
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
				
				PaintingListDTO painting = PaintingListDTO.builder()
					.id(id).url(url)
					.label(withoutLanguageCode(label))
					.description(withoutLanguageCode(description))
					.build();
				paintings.add(painting);
			}
			
			return paintings;
		}
	}
	
	public void createPainting(PaintingDTO painting, HttpServletRequest request) {
		try {
			ApiConnection connection = loginService.getApiConnection(request);
			WikibaseDataEditor wbde = new WikibaseDataEditor(connection, wikidataProperties.getUrl());
			wbde.setMaxLagFirstWaitTime(1000000);
			
			ItemIdValue noid = ItemIdValue.NULL;
			PropertyIdValue creatorPropId
				= (PropertyIdValue) PropertyIdValueImpl.fromId(CREATOR, wikidataProperties.getUrl());
			PropertyIdValue invPropId
				= (PropertyIdValue) PropertyIdValueImpl.fromId(INVENTORY_NUMBER, wikidataProperties.getUrl());
			PropertyIdValue locationPropId
				= (PropertyIdValue) PropertyIdValueImpl.fromId(LOCATION, wikidataProperties.getUrl());
			PropertyIdValue materialPropId
				= (PropertyIdValue) PropertyIdValueImpl.fromId(MATERIAL, wikidataProperties.getUrl());
			
			ItemDocumentBuilder builder = ItemDocumentBuilder.forItemId(noid)
				.withLabel(new MonolingualTextValueImpl(painting.getLabel(), "en"))
				.withDescription(new MonolingualTextValueImpl(painting.getDescription(), "en"));
			
			if (painting.getCreator() != null) {
				Statement creatorStatement = StatementBuilder.forSubjectAndProperty(noid, creatorPropId)
				.withValue(Datamodel.makeItemIdValue(painting.getCreator(), wikidataProperties.getUrl()))
				.build();
				
				builder = builder.withStatement(creatorStatement);
			}
			
			if (painting.getInvnum() != null) {
				Statement invStatement = StatementBuilder.forSubjectAndProperty(noid, invPropId)
				.withValue(Datamodel.makeStringValue(painting.getInvnum()))
				.build();
				
				builder = builder.withStatement(invStatement);
			}
			
			if (painting.getLocation() != null) {
				Statement locationStatement = StatementBuilder.forSubjectAndProperty(noid, locationPropId)
				.withValue(Datamodel.makeItemIdValue(painting.getLocation(), wikidataProperties.getUrl()))
				.build();
				
				builder = builder.withStatement(locationStatement);
			}
			
			if (painting.getMaterials() != null) {
				for (String materialId : painting.getMaterials()) {
					Statement materialStatement = StatementBuilder.forSubjectAndProperty(noid, materialPropId)
							.withValue(Datamodel.makeItemIdValue(materialId, wikidataProperties.getUrl()))
							.build();
					builder = builder.withStatement(materialStatement);
				}
			} 
			builder = builder.withRevisionId(0);
			ItemDocument newItemDocument = wbde.createItemDocument(builder.build(),
					"Painting creation test", null);

			ItemIdValue newItemId = newItemDocument.getEntityId();
			System.out.println("*** Successfully created a new item "
					+ newItemId.getId()
					+ " (see https://test.wikidata.org/w/index.php?title="
					+ newItemId.getId() + "&oldid="
					+ newItemDocument.getRevisionId() + " for this version)");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updatePainting(PaintingDTO painting, HttpServletRequest request) {
		try {
			ApiConnection connection = loginService.getApiConnection(request);
			WikibaseDataEditor wbde = new WikibaseDataEditor(connection, wikidataProperties.getUrl());
			WikibaseDataFetcher wbdf = new WikibaseDataFetcher(connection, wikidataProperties.getUrl());
			
			EntityDocument entity = wbdf.getEntityDocument(painting.getId());
			
			if (entity instanceof ItemDocument) {
				ItemDocument item = (ItemDocument) entity;
				
				ItemIdValue subjectId = (ItemIdValue) ItemIdValueImpl
						.fromId(painting.getId(), wikidataProperties.getUrl());
				
				item = item.withLabel(
						new MonolingualTextValueImpl(painting.getLabel(), "en"));
				item = item.withDescription(
						new MonolingualTextValueImpl(painting.getDescription(), "en"));
				
				PropertyIdValue creatorPropId
					= (PropertyIdValue) PropertyIdValueImpl.fromId(CREATOR, wikidataProperties.getUrl());
				PropertyIdValue invPropId
					= (PropertyIdValue) PropertyIdValueImpl.fromId(INVENTORY_NUMBER, wikidataProperties.getUrl());
				PropertyIdValue locationPropId
					= (PropertyIdValue) PropertyIdValueImpl.fromId(LOCATION, wikidataProperties.getUrl());
				PropertyIdValue materialPropId
					= (PropertyIdValue) PropertyIdValueImpl.fromId(MATERIAL, wikidataProperties.getUrl());
				
				if (painting.getCreator() != null) {
					Statement creatorStatement = StatementBuilder.forSubjectAndProperty(subjectId, creatorPropId)
					.withValue(Datamodel.makeItemIdValue(painting.getCreator(), wikidataProperties.getUrl()))
					.build();
					
					Set<String> oldStatements = item.findStatementGroup(creatorPropId)
							.stream().map(Statement::getStatementId).collect(Collectors.toSet());
					
					item = item.withoutStatementIds(oldStatements).withStatement(creatorStatement);
				}
				
				if (painting.getInvnum() != null) {
					Statement invStatement = StatementBuilder.forSubjectAndProperty(subjectId, invPropId)
					.withValue(Datamodel.makeStringValue(painting.getInvnum()))
					.build();
					
					Set<String> oldStatements = item.findStatementGroup(invPropId)
							.stream().map(Statement::getStatementId).collect(Collectors.toSet());
					
					item = item.withoutStatementIds(oldStatements).withStatement(invStatement);
				}
				
				if (painting.getLocation() != null) {
					Statement locationStatement = StatementBuilder.forSubjectAndProperty(subjectId, locationPropId)
					.withValue(Datamodel.makeItemIdValue(painting.getLocation(), wikidataProperties.getUrl()))
					.build();
					
					Set<String> oldStatements = item.findStatementGroup(locationPropId)
							.stream().map(Statement::getStatementId).collect(Collectors.toSet());
					
					item = item.withoutStatementIds(oldStatements).withStatement(locationStatement);
				}
				
				if (painting.getMaterials() != null) {
					Set<String> oldStatements = item.findStatementGroup(materialPropId)
							.stream().map(Statement::getStatementId).collect(Collectors.toSet());
					item = item.withoutStatementIds(oldStatements);
					
					for (String materialId : painting.getMaterials()) {
						Statement materialStatement = StatementBuilder.forSubjectAndProperty(subjectId, materialPropId)
								.withValue(Datamodel.makeItemIdValue(materialId, wikidataProperties.getUrl()))
								.build();
						item = item.withStatement(materialStatement);
					}
				}
				
				wbde.editItemDocument(item, false, "Painting update test", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public PaintingDTO getPainting(String entityId) {
		try {
			ApiConnection connection = BasicApiConnection.getWikidataApiConnection();
			WikibaseDataFetcher wbdf = new WikibaseDataFetcher(connection, wikidataProperties.getUrl());
			
			EntityDocument entity = wbdf.getEntityDocument(entityId);
			
			if (entity instanceof ItemDocument) {
				ItemDocument item = (ItemDocument) entity;
				String label = item.findLabel("en");
				String description = item.findDescription("en");
				
				Statement creatorStatement = getStatement(CREATOR, item);
				Statement creationStatement = getStatement(CREATION_TIME, item);
				Statement inventoryStatement = getStatement(INVENTORY_NUMBER, item);
				Statement locationStatement = getStatement(LOCATION, item);
				
				List<Statement> materialStatements = item.findStatementGroup(MATERIAL).getStatements();
				
				String creator = creatorStatement == null ? null 
						: ((ItemIdValue) creatorStatement.getValue()).getId();
				String ctime = creationStatement == null ? null
						: ((TimeValue) creationStatement.getValue()).getYear() + "";
				String inventoryId = creationStatement == null ? null
						: ((StringValue) inventoryStatement.getValue()).getString();
				String location = locationStatement == null ? null
						: ((ItemIdValue) locationStatement.getValue()).getId();
				List<String> materials = materialStatements.stream().map(statement -> 
					((ItemIdValue) statement.getValue()).getId()
				).collect(Collectors.toList());
				
				return PaintingDTO.builder().creator(creator).ctime(ctime).invnum(inventoryId)
				.materials(materials).id(entityId).description(description)
				.location(location).label(label).build();
			}
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public PaintingWithMetadata getPaintingWithMetadata(String entityId) {
		try {
			ApiConnection connection = BasicApiConnection.getWikidataApiConnection();
			WikibaseDataFetcher wbdf = new WikibaseDataFetcher(connection, wikidataProperties.getUrl());
			
			EntityDocument entity = wbdf.getEntityDocument(entityId);
			
			if (entity instanceof ItemDocument) {
				ItemDocument item = (ItemDocument) entity;
				String label = item.findLabel("en");
				String description = item.findDescription("en");
				
				Statement creatorStatement = getStatement(CREATOR, item);
				Statement creationStatement = getStatement(CREATION_TIME, item);
				Statement inventoryStatement = getStatement(INVENTORY_NUMBER, item);
				Statement locationStatement = getStatement(LOCATION, item);
				
				List<Statement> materialStatements = item.findStatementGroup(MATERIAL).getStatements();
				
				ItemMetaData creator = creatorStatement == null ? null 
						: getItemMetaData(((ItemIdValue) creatorStatement.getValue()).getId(), wbdf);
				String ctime = creationStatement == null ? null
						: ((TimeValue) creationStatement.getValue()).getYear() + "";
				String inventoryId = creationStatement == null ? null
						: ((StringValue) inventoryStatement.getValue()).getString();
				ItemMetaData location = locationStatement == null ? null
						: getItemMetaData(((ItemIdValue) locationStatement.getValue()).getId(), wbdf);
				
				List<ItemMetaData> materials = new ArrayList<>();
				for (Statement statement : materialStatements) {
					materials.add(
							getItemMetaData(((ItemIdValue) statement.getValue()).getId(), wbdf)
					);
				}
				
				return PaintingWithMetadata.builder().creator(creator).ctime(ctime).invnum(inventoryId)
				.materials(materials).id(entityId).description(description)
				.location(location).label(label).url(wikidataProperties.getUrl() + entityId).build();
			}
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	private ItemMetaData getItemMetaData(String itemId, WikibaseDataFetcher wbdf) throws MediaWikiApiErrorException, IOException {
		wbdf.getFilter().excludeAllProperties();
		wbdf.getFilter().setLanguageFilter(Collections.singleton("en"));
		
		EntityDocument entity = wbdf.getEntityDocument(itemId);
		ItemDocument item = (ItemDocument) entity;
		
		return ItemMetaData.builder()
			.id(itemId).description(item.findDescription("en"))
			.label(item.findLabel("en"))
			.url(wikidataProperties.getUrl() + itemId).build();
	}
	
	private Statement getStatement(String statementId, ItemDocument item) {
		return item.findStatementGroup(statementId).stream().findFirst().orElse(null);
	}
	
	public String withoutLanguageCode(String raw) {
		if(raw !=null && raw.startsWith("\"") && raw.endsWith("\"@en")) {
			return raw.substring(1, raw.length() - 4);
		}
		
		return raw;
	}
}
