package hu.bme.aut.wikidataeditor.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wikidata.wdtk.util.WebResourceFetcherImpl;
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection;
import org.wikidata.wdtk.wikibaseapi.LoginFailedException;

import hu.bme.aut.wikidataeditor.model.Credentials;
import hu.bme.aut.wikidataeditor.property.WikidataProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoginService {
	
	@Autowired
	WikidataProperties properties;
	
	private static final String API_CONNECTION = "API_CONNECTION";
	
	public boolean login(Credentials cred, HttpServletRequest request){
		WebResourceFetcherImpl
			.setUserAgent("Wikidata Toolkit WikidataEditor");

		try {
			
			BasicApiConnection connection = properties.getUrl().contains("test")
					? BasicApiConnection.getTestWikidataApiConnection()
					: BasicApiConnection.getWikidataApiConnection();
			
			connection.login(cred.getUsername(), cred.getPassword());
			
			if (connection.isLoggedIn()){
				request.getSession().setAttribute(API_CONNECTION, connection);
				return true;
			}
			
		} catch(LoginFailedException e) {
			log.warn("LoginFailedException occured");
		}
		return false;
	}
	
	public boolean isLoggedIn(HttpServletRequest request) {
		BasicApiConnection connection = getApiConnection(request);
		log.info(connection.toString());
		return connection != null && connection.isLoggedIn();
	}
	
	public BasicApiConnection getApiConnection(HttpServletRequest request) {
		return (BasicApiConnection) request.getSession().getAttribute(API_CONNECTION);
	}
}
