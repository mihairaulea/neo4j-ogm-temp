package org.neo4j.ogm.session.event;

/**
 * Created by Mihai Raulea on 3/11/2016.
 */
public class SaveEvent implements Event {

    public static String LIFECYCLE;
    public Object affectedObject;

    public static final String PRE  = "preSave";
    public static final String POST = "postSave";

    public Object getTargetObject() {
        return affectedObject;
    }

}
