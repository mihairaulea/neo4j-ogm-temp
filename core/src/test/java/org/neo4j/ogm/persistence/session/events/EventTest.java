package org.neo4j.ogm.persistence.session.events;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.EventListener;
import org.neo4j.ogm.session.event.SaveEvent;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Mihai Raulea on 3/14/2016.
 */
public class EventTest {

    private Session session;
    Document a;
    Document b;
    Document c;
    Document d;
    Folder f;

    EventListenerTest eventListenerTest;

    @Before
    public void init() throws IOException {
        // each test should instantiate a new one
        eventListenerTest = null;
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.filesystem");
        session = sessionFactory.openSession();

        a = new Document();
        a.setName("a");

        b = new Document();
        b.setName("b");

        // will this be included in the EntityGraphMapper? it has no relationship to folder f
        c = new Document();
        c.setName("c");

        d = new Document();
        d.setName("d");

        f = new Folder();
        f.setName("f");

        f.getDocuments().add(a);
        f.getDocuments().add(b);
        f.getDocuments().add(c);

        a.setFolder(f);
        b.setFolder(f);

        session.save(f);
    }
    // TODO: write tests to assert that the objects retrieved from the context are the expected ones
    @Test
    public void testAddOneNode() {
        eventListenerTest = new EventListenerTest(2);
        session.register(eventListenerTest);

        Document e = new Document();
        e.setName("e");

        session.save(e);

        Class[] expectedObjectTypes = new Class[2];
        expectedObjectTypes[0] = Document.class;
        expectedObjectTypes[1] = Document.class;

        testExpectedNumberOfEventsInQueue(eventListenerTest,2);
        testExpectedSequenceTargetObjectTypes(eventListenerTest, expectedObjectTypes);
    }

    @Test
    public void testAddMultipleNodes() {
        // we should only be saving one node, now
        session.save(a);
        session.save(b);
        session.save(c);
        session.save(d);

        eventListenerTest = new EventListenerTest(2);
        session.register(eventListenerTest);

        Document e = new Document();
        e.setName("e");

        session.save(e);

        Document f = new Document();
        e.setName("f");

        session.save(e);
    }

    @Test
    public void testAlterMultipleNodes() {

    }

    @Test
    public void testAddOneRelationship() {
        // we should only be saving one relationship, now
        session.save(a);
        session.save(b);
        session.save(c);
        session.save(d);


        eventListenerTest = new EventListenerTest(1);
        session.register(eventListenerTest);

        // c did not have a folder assigned in the init()
        c.setFolder(f);
        session.save(c);

        // a SaveEvent with a TransientRelationship is expected inside
        Event capturedEvent = eventListenerTest.eventsCaptured[0];
        //assertNotNull(capturedEvent);
        //assertTrue(capturedEvent instanceof SaveEvent);
    }

    @Test
    public void testAddMultipleRelationships() {

    }

    @Test
    public void testAlterOneRelationship() {

    }

    @Test
    public void testAlterMultipleRelationships() {

    }

    class EventListenerTest implements EventListener {

        public Event[] eventsCaptured;
        public int currentIndex = 0;

        public EventListenerTest(int noOfExpectedEvents) {
            eventsCaptured = new Event[noOfExpectedEvents];
            currentIndex = 0;
        }

        @Override
        public void update(Event event) {
            eventsCaptured[currentIndex++] = event;
            System.out.println("caught an event of type "+event.toString());
        }

    }

    @Test
    public void noEventsShouldFire() {
        this.eventListenerTest = new EventListenerTest(0);
        session.register(eventListenerTest);

        session.save(f);
        Document aa = session.load(Document.class, a.getId());
        Document bb = session.load(Document.class, b.getId());
        System.out.println(aa.getName());
        System.out.println(bb.getName());
        System.out.println(f.getName());

        testExpectedNumberOfEventsInQueue(eventListenerTest, 8);
    }

    @Test
    public void eventIntegrationTest() {
        this.eventListenerTest = new EventListenerTest(8);
        session.register(eventListenerTest);

        a.setName("newA");
        b.setName("newB");
        c.setName("newC");
        f.setName("newF");

        session.save(f);
        Document aa = session.load(Document.class, a.getId());
        Document bb = session.load(Document.class, b.getId());
        System.out.println(aa.getName());
        System.out.println(bb.getName());
        System.out.println(f.getName());

        testExpectedNumberOfEventsInQueue(eventListenerTest, 8);
    }

    private void testExpectedNumberOfEventsInQueue(EventListenerTest eventListener, int noOfExpectedEvents) {
        assertTrue(eventListener.currentIndex == noOfExpectedEvents);
    }

    // this is not usable when related entities are saved, because the order could be arbitrary
    private void testExpectedSequenceTargetObjectTypes(EventListenerTest eventListener, Class[] types) {
        testExpectedNumberOfEventsInQueue(eventListener, types.length);
        for(int i=0;i<eventListener.eventsCaptured.length;i++)
            assertTrue(types[i].isInstance(eventListener.eventsCaptured[i].getTargetObject()));
    }

    private void testCountOfExpectedTargetObjects(EventListenerTest eventListener, TargetObjectCount[] targetObjectCountArray) {
        Class currentClass = null;
        for(int i=0;i<targetObjectCountArray.length;i++) {
            assertContainsObjects(eventListener,targetObjectCountArray[i]);
        }
    }

    private void assertContainsObjects(EventListenerTest eventListener, TargetObjectCount targetObjectCountArray) {
        int numberOfObjectsOfTypeFound = 0;
        for(int i=0;i<eventListener.eventsCaptured.length;i++) {
            if(targetObjectCountArray.targetObjectType.isInstance(eventListener.eventsCaptured[i].getTargetObject()))
                numberOfObjectsOfTypeFound++;
        }
        assertTrue(numberOfObjectsOfTypeFound == targetObjectCountArray.count);
    }

    class TargetObjectCount {
        public Class targetObjectType;
        public int count;
    }

}
