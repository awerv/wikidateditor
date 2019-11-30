package hu.bme.aut.wikidataeditor.wrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gson.*;

import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.Claim;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchWrapper {

    public static String search(String payload) {

        String phrase = payload.split("=")[1];

        System.out.println(phrase);

        ArrayList<SearchResult> result = new ArrayList<SearchResult>();

        try {
            WikibaseDataFetcher wbdf = WikibaseDataFetcher.getWikidataDataFetcher();
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
                                result.add(new SearchResult(res.getLabel(), res.getEntityId(), res.getUrl(),
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

class SearchInput {
    private String phrase;

    SearchInput(String phrase) {
        this.setPhrase(phrase);
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }
}

class SearchResult {
    private String label;
    private String entityId;
    private String url;
    private String description;

    SearchResult(String label, String entityId, String url, String description) {
        this.setLabel(label);
        this.setEntityId(entityId);
        this.setUrl("https:"+url);
        this.setDescription(description);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}




