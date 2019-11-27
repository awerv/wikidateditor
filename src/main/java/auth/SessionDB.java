package main.java.auth;

import java.util.HashMap;

import org.wikidata.wdtk.wikibaseapi.BasicApiConnection;

public class SessionDB{
    private HashMap<String, BasicApiConnection> session_map;
    private static SessionDB sessions;

    private SessionDB(){
        session_map = new HashMap<String, BasicApiConnection>();
    }

    public static SessionDB getInstance(){
        if(sessions == null){
            synchronized(SessionDB.class){
                sessions = new SessionDB();
            }
        }
        return sessions;
    }

    public void put(String cookie, BasicApiConnection conn){
        synchronized(SessionDB.class){
            session_map.put(cookie, conn);
        }
    }

    public BasicApiConnection get(String cookie){
        synchronized(SessionDB.class){
            return session_map.get(cookie);
        }
    }

    public int size(){
        synchronized(SessionDB.class){
            return session_map.size();
        }
    }

    public boolean containsKey(String key){
        synchronized(SessionDB.class){
            return session_map.containsKey(key);
        }
    }
}