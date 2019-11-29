package main.java.wrappers;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.xml.ws.ProtocolException;

import java.nio.charset.StandardCharsets;

import org.wikidata.wdtk.wikibaseapi.ApiConnection;
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection;
import org.wikidata.wdtk.util.WebResourceFetcherImpl;
import org.wikidata.wdtk.wikibaseapi.LoginFailedException;
import  lombok.extern.slf4j.Slf4j;

import main.java.auth.Credentials;

// test user:
//      edittestuser
//      edittestrandompw

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




/*
    public static String login(Credentials cred){
        System.out.println("CSRF token retrieved: " + token);

        try{

            String line;
            StringBuilder result = new StringBuilder();
            StringBuilder params = new StringBuilder();
            params.append("action=clientlogin");
            params.append("&username=");
            params.append(username);
            params.append("&password=");
            params.append(password);
            params.append("&loginreturnurl=");
            params.append(MOCKURL);
            params.append("&logintoken=");
            params.append(token);
            params.append("&format=json");

            System.out.println(params.toString());

            final URL url = new URL(APIURL);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            wr.write(params.toString());
            wr.flush();
            wr.close();

            conn.connect();

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();

            System.out.println(result.toString());
        } catch(IOException e){
            e.printStackTrace();
        } catch(ProtocolException e){
            e.printStackTrace();
        }
        return null;
    }

    public void test(){
        String token = getCSRFToken().replaceAll("\"", "");
        String encodedtoken = token.substring(0, token.length()-1);
        login("edittestuser", "edittestrandompw", "6427f2fdad182320420c2f5178048b2b5dd869ec+\\");
    }
*/            


}