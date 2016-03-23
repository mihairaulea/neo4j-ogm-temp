package org.neo4j.ogm.persistence.session.events;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.EventListener;
import java.io.IOException;
import static org.junit.Assert.assertTrue;

/**
 * Created by Mihai Raulea on 3/14/2016.
 */
@Ignore
public class EventTest {

    private Session session;
    Document a;
    Document b;
    Document c;
    Document d;
    Folder folder;

    EventListenerTest eventListenerTest;

    @Before
    public void init() throws IOException {
        // each test should instantiate a new one
        eventListenerTest = null;
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.filesystem");
        session = sessionFactory.openSession();
        session.purgeDatabase();
        a = new Document();
        a.setName("a");

        b = new Document();
        b.setName("b");

        // will this be included in the EntityGraphMapper? it has no relationship to folder f
        c = new Document();
        c.setName("c");

        d = new Document();
        d.setName("d");

        folder = new Folder();
        folder.setName("folder");

        folder.getDocuments().add(a);
        folder.getDocuments().add(b);
        folder.getDocuments().add(c);

        a.setFolder(folder);
        b.setFolder(folder);
        c.setFolder(folder);

        session.save(folder);
    }

    // TODO: write tests to assert that the objects retrieved from the context are the expected ones
    @Test
    public void noEventsShouldFire() {
        this.eventListenerTest = new EventListenerTest(0);
        session.register(eventListenerTest);

        session.save(folder);
        Document aa = session.load(Document.class, a.getId());
        Document bb = session.load(Document.class, b.getId());

        testExpectedNumberOfEventsInQueue(eventListenerTest, 0);
    }

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
        eventListenerTest = new EventListenerTest(6);
        session.register(eventListenerTest);

        Document e = new Document();
        e.setName("e");
        session.save(e);

        Document f = new Document();
        f.setName("f");
        session.save(f);

        Document g = new Document();
        g.setName("g");
        session.save(g);

        Class[] expectedObjectTypes = new Class[6];
        for(int i=0;i<6;i++)
            expectedObjectTypes[i] = Document.class;

        testExpectedNumberOfEventsInQueue(eventListenerTest,6);
        testExpectedSequenceTargetObjectTypes(eventListenerTest, expectedObjectTypes);
    }

    @Test
    public void testAlterMultipleNodes() {
        eventListenerTest = new EventListenerTest(6);
        session.register(eventListenerTest);

        Document e = new Document();
        e.setName("newE");
        session.save(e);

        Document f = new Document();
        f.setName("newF");
        session.save(f);

        Document g = new Document();
        g.setName("newG");
        session.save(g);

        Class[] expectedObjectTypes = new Class[6];
        for(int i=0;i<6;i++)
            expectedObjectTypes[i] = Document.class;

        testExpectedNumberOfEventsInQueue(eventListenerTest,6);
        testExpectedSequenceTargetObjectTypes(eventListenerTest, expectedObjectTypes);
    }

    @Test
    public void testAddAndAlterMultipleUnconnectedNodes() {
        eventListenerTest = new EventListenerTest(8);
        session.register(eventListenerTest);

        // these nodes are new
        Document e = new Document();
        e.setName("newE");
        session.save(e);

        Document f = new Document();
        f.setName("newF");
        session.save(f);

        Document g = new Document();
        g.setName("newG");
        session.save(g);

        // this node is not connected to any other entities
        d.setName("newD");
        session.save(d);

        Class[] expectedObjectTypes = new Class[8];
        for(int i=0;i<8;i++)
            expectedObjectTypes[i] = Document.class;

        testExpectedNumberOfEventsInQueue(eventListenerTest,8);
        testExpectedSequenceTargetObjectTypes(eventListenerTest, expectedObjectTypes);
    }

    @Test
    public void testAddAndAlterConnectedNode() {
        eventListenerTest = new EventListenerTest(8);
        session.register(eventListenerTest);

        Document e = new Document();
        e.setName("newE");
        session.save(e);

        Document f = new Document();
        f.setName("newF");
        session.save(f);

        Document g = new Document();
        g.setName("newG");
        session.save(g);

        // even though the node is connected, the connected nodes are not dirty, so no additional events should fire
        a.setName("newA");
        session.save(a);

        Class[] expectedObjectTypes = new Class[8];
        for(int i=0;i<8;i++)
            expectedObjectTypes[i] = Document.class;

        testExpectedNumberOfEventsInQueue(eventListenerTest,8);
        testExpectedSequenceTargetObjectTypes(eventListenerTest, expectedObjectTypes);
    }

    @Test
    public void testAddAndAlterMultipleConnectedNode() {
        int noOfExpectedEvents = 6;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);
        /* debugging, turn these back on
        Document e = new Document();
        e.setName("newE");
        session.save(e);

        Document f = new Document();
        f.setName("newF");
        session.save(f);

        Document g = new Document();
        g.setName("newG");
        session.save(g);
        */
        // even though the node is connected, the connected nodes are not dirty, so the context registry should only contain 1 node at a time
        a.setName("newA");
        session.save(a);

        b.setName("newB");
        session.save(b);

        c.setName("newC");
        session.save(c);

        Class[] expectedObjectTypes = new Class[noOfExpectedEvents];
        for(int i=0;i<noOfExpectedEvents;i++)
            expectedObjectTypes[i] = Document.class;

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        testExpectedSequenceTargetObjectTypes(eventListenerTest, expectedObjectTypes);
    }

    @Test
    public void testAddOneRelationship() {
        eventListenerTest = new EventListenerTest(1);
        session.register(eventListenerTest);

        // c did not have a folder assigned in the init()
        c.setFolder(folder);
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
    public void eventIntegrationTest() {
        this.eventListenerTest = new EventListenerTest(8);
        session.register(eventListenerTest);

        a.setName("newA");
        b.setName("newB");
        c.setName("newC");
        folder.setName("newFolder");

        session.save(folder);
        Document aa = session.load(Document.class, a.getId());
        Document bb = session.load(Document.class, b.getId());
        System.out.println(aa.getName());
        System.out.println(bb.getName());
        System.out.println(folder.getName());

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

    @After
    public void clean() throws IOException {
        session.purgeDatabase();
    }

}
