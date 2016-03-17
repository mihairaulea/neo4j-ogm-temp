package org.neo4j.ogm.session.event;

/**
 * Created by Mihai Raulea on 3/15/2016.
 */
public interface Observer {

    void register(EventListener eventListener);
    void notifyListeners(Event event);

}
