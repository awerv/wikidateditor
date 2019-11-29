package hu.bme.aut.wikidataeditor.wrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.Claim;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

public class SearchWrapper {
    public static HashSet<WbSearchEntitiesResult> search(String phrase){

        HashSet<WbSearchEntitiesResult> result = new HashSet<WbSearchEntitiesResult>();

        try{
            WikibaseDataFetcher wbdf = WikibaseDataFetcher.getWikidataDataFetcher();
            wbdf.getFilter().setSiteLinkFilter(Collections.singleton("enwiki"));
            wbdf.getFilter().setLanguageFilter(Collections.singleton("en"));
            Set<PropertyIdValue> ids = new HashSet<>();
            ids.add(Datamodel.makeWikidataPropertyIdValue("P31"));
            wbdf.getFilter().setPropertyFilter(ids);


            for(WbSearchEntitiesResult res : wbdf.searchEntities(phrase)){
                EntityDocument ed = wbdf.getEntityDocument(res.getTitle());
                if(ed instanceof ItemDocument){
                    Iterator<Statement> it = ((ItemDocument) ed).getAllStatements();
                    while(it.hasNext()){
                        Claim c = it.next().getClaim();
                        // check if the property is instance of and the value of it is painting
                        if(c.getMainSnak().getPropertyId().getId().equals("P31")){
                            if(c.getMainSnak().getValue().toString().equals("http://www.wikidata.org/entity/Q3305213 (item)")){
                                result.add(res);
                            }
                        }

                    }
                }

                
            }

        } catch(MediaWikiApiErrorException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }
}