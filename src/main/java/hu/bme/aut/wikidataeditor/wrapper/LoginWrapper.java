package hu.bme.aut.wikidataeditor.wrapper;

import hu.bme.aut.wikidataeditor.auth.Credentials;

import org.wikidata.wdtk.wikibaseapi.BasicApiConnection;
import org.wikidata.wdtk.util.WebResourceFetcherImpl;
import org.wikidata.wdtk.wikibaseapi.LoginFailedException;
import  lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginWrapper{

    public static BasicApiConnection login(Credentials cred){
        WebResourceFetcherImpl
        .setUserAgent("Wikidata Toolkit WikidataEditor");

        try{
            BasicApiConnection connection = BasicApiConnection.getWikidataApiConnection();
            connection.login(cred.getUsername(), cred.getPassword());

            if(connection.isLoggedIn()){
                return connection;
            }
        } catch(LoginFailedException e){
            log.warn("LoginFailedException occured");
        }
        return null;
    }
}