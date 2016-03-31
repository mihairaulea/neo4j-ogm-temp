package org.neo4j.ogm.session.event;

import java.util.List;

/**
 * Calling session.delete(entity) on an object that is not persisted will fire no events
 *
 * Calling session.delete(entity) on an entity will fire two DeleteEvent s, before and after persisting; getTargetObjects() retrieves a list with only one object.
 *
 * Calling session.delete(<? extends Collection>) will fire a sequence of DeleteEvent s, before and after persisting; getTargetObjects() retrieves the list of all affected entities.
 */

public class DeleteEvent implements Event {

    public static String LIFECYCLE;
    public Object affectedObject;

    public static final String PRE  = "preSave";
    public static final String POST = "postSave";

    public Object getTargetObject() {
        return affectedObject;
    }
}
