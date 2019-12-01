package hu.bme.aut.wikidataeditor.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.Claim;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.wikibaseapi.ApiConnection;
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import com.google.gson.Gson;

import hu.bme.aut.wikidataeditor.model.SearchResult;
import hu.bme.aut.wikidataeditor.property.WikidataProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SearchService {
	
	@Autowired
	WikidataProperties wikidataProperties;
	
	public String search(String payload) {

        String phrase = payload.split("=")[1];

        System.out.println(phrase);

        ArrayList<SearchResult> result = new ArrayList<SearchResult>();

        try {
        	ApiConnection connection = BasicApiConnection.getWikidataApiConnection();
        	
            WikibaseDataFetcher wbdf = 
            		new WikibaseDataFetcher(connection, wikidataProperties.getUrl());
            wbdf.getFilter().setSiteLinkFilter(Collections.singleton("enwiki"));
            wbdf.getFilter().setLanguageFilter(Collections.singleton("en"));
            Set<PropertyIdValue> ids = new HashSet<>();
            ids.add(Datamodel.makeWikidataPropertyIdValue("P31"));
            wbdf.getFilter().setPropertyFilter(ids);

            for (WbSearchEntitiesResult res : wbdf.searchEntities(phrase)) {
                EntityDocument ed = wbdf.getEntityDocument(res.getTitle());
                if (ed instanceof ItemDocument) {
                    Iterator<Statement> it = ((ItemDocument) ed).getAllStatements();
                    while (it.hasNext()) {
                        Claim c = it.next().getClaim();
                        // check if the property is instance of and the value of it is painting
                        if (c.getMainSnak().getPropertyId().getId().equals("P31")) {
                            if (c.getMainSnak().getValue().toString()
                                    .equals("http://www.wikidata.org/entity/Q3305213 (item)")) {
                                result.add(new SearchResult(res.getLabel(), res.getEntityId(), "https:" + res.getUrl(),
                                        res.getDescription()));
                            }
                        }

                    }
                }

            }

        } catch (MediaWikiApiErrorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String jsonStr = new Gson().toJson(result);
        log.info(jsonStr);
        return jsonStr;
    }
}
