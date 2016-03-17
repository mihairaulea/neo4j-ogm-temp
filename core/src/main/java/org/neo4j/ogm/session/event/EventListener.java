package org.neo4j.ogm.session.event;

/**
 * Created by Mihai Raulea on 3/16/2016.
 */
public interface EventListener {

    void update(Event event);

}
