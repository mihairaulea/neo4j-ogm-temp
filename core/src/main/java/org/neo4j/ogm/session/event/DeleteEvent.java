package org.neo4j.ogm.session.event;

import java.util.List;

/**
 * Created by Mihai Raulea on 3/11/2016.
 */
public class DeleteEvent implements Event {

    public static String LIFECYCLE;
    public List<Object> affectedObjects;

    public static final String PRE  = "preSave";
    public static final String POST = "postSave";

    public List<Object> getTargetObjects() {
        return affectedObjects;
    }
}
