package hu.bme.aut.wikidataeditor.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.wikidata.wdtk.util.WebResourceFetcherImpl;
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection;
import org.wikidata.wdtk.wikibaseapi.LoginFailedException;

import hu.bme.aut.wikidataeditor.auth.Credentials;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoginService {
	
	private static final String API_CONNECTION = "API_CONNECTION";
	
	public boolean login(Credentials cred, HttpServletRequest request){
		WebResourceFetcherImpl
			.setUserAgent("Wikidata Toolkit WikidataEditor");

		try {
			BasicApiConnection connection = BasicApiConnection.getWikidataApiConnection();
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
		return connection != null && connection.isLoggedIn();
	}
	
	public BasicApiConnection getApiConnection(HttpServletRequest request) {
		return (BasicApiConnection) request.getSession().getAttribute(API_CONNECTION);
	}
}
