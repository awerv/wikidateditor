package main.java.application;

import main.java.auth.Credentials;
import main.java.auth.SessionDB;

import main.java.miscs.RandomString;

import main.java.wrappers.SearchWrapper;
import main.java.wrappers.LoginWrapper;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import javax.servlet.http.Cookie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

import org.wikidata.wdtk.wikibaseapi.BasicApiConnection;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

@SpringBootApplication
@RestController
@Slf4j
public class Application {

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(@CookieValue(name = "Wikidatacookie") String cookie, @RequestBody String payload) {
        log.info(payload);

        SessionDB sessions = SessionDB.getInstance();

        Credentials cred = new Gson().fromJson(payload, Credentials.class);

        log.info("Recieved authentication request for " + cred.getUsername());

        if(sessions.containsKey(cookie)){
            log.info(cred.getUsername() + " is already logged in");
            return "ALREADYIN";
        }

        BasicApiConnection conn = LoginWrapper.login(cred);

        if(conn == null){
            log.info("Authentication of " + cred.getUsername() + "failed");
            return "AUTHFAIL";
        } else {
            log.info("Authentication of " + cred.getUsername() + "was successful");
            sessions.put(cookie, conn);

            return "AUTHOK";
        }
    }

    @RequestMapping(value="/search")
    @ResponseBody
    public String search(@RequestParam String phrase){

        log.info("Search started for " + phrase);
        Set<WbSearchEntitiesResult> res = SearchWrapper.search(phrase);
        log.info("Number of results for " + phrase + ": " + res.size());
        return "OK";
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}