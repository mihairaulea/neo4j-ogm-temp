package org.neo4j.ogm.session.event;

import java.util.List;

/**
 * Calling session.save(entity) on an object that is not dirty(the in-memory representation of the object is the same as the object in the database) will fire no events
 *
 * Calling save(entity) on a dirty entity will fire two SaveEvent s, before and after updating; getTargetObjects() retrieves a list with only one object.
 * If dirty connected entities are found, then each dirty entity will fire SaveEvent in the same manner
 *
 * Calling session.save(<? extends Collection>) will fire just two SaveEvent s, before and after persisting; getTargetObjects() retrieves the list of all affected entities.
 * The connected dirty objects rule applies.
 *
 * Calling session.save(entity) on an entity that has new relationships will fire two SaveEvent s, before and after persisting; getTargetObjects() retrieves the list of TransientRelationship, one for each new relationship created
 * The connected dirty objects/entities rule applies.
 *
 * Calling session.save(relationshipEntity) altering a RelationshipEntity will fire a SaveEvent before and after persisting; each event will contain the relationship object, and a TransientRelationship. This is because internally, the OGM first deletes the relationship, and then recreates it.
 * The connected dirty objects/entities rule applies.
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
