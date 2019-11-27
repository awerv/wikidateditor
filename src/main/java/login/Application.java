package main.java.login;

import main.java.auth.Credentials;
import main.java.auth.Authenticator;
import main.java.auth.SessionDB;

import main.java.login.RandomString;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;

import  lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

import org.wikidata.wdtk.wikibaseapi.BasicApiConnection;

@SpringBootApplication
@RestController
@Slf4j
public class Application {

    @RequestMapping(value = "/")
    @ResponseBody
    public String home(@RequestBody String payload) {
        return "OK";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(@CookieValue(name = "Wikidatacookie") String cookie, @RequestBody String payload) {
        log.info(payload);

        SessionDB sessions = SessionDB.getInstance();

        String user = payload.split("&")[0].split("=")[1];
        String pass = payload.split("&")[1].split("=")[1];
//        Credentials cred = new Gson().fromJson(payload, Credentials.class);
        Credentials cred = new Credentials(user, pass);

        log.info(user);
        log.info(pass);

        log.info("Recieved authentication request for " + cred.getUsername());

        if(sessions.containsKey(cookie)){
            log.info(cred.getUsername() + " is already logged in");
            return "ALREADYIN";
        }

        BasicApiConnection conn = Authenticator.login(cred);

        if(conn == null){
            log.info("Authentication of " + cred.getUsername() + "failed");
            return "AUTHFAIL";
        } else {
            log.info("Authentication of " + cred.getUsername() + "was successful");
            sessions.put(cookie, conn);

            return "AUTHOK";
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}