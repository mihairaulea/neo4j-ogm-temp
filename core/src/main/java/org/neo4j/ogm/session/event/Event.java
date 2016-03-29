package org.neo4j.ogm.session.event;

import java.util.List;

/**
 * Created by Mihai Raulea on 3/15/2016.
 */
public interface Event {

    List<Object> getTargetObjects();

}
