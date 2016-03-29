package org.neo4j.ogm.persistence.session.events;

import org.junit.*;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.Knows;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.EventListener;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by Mihai Raulea on 3/14/2016.
 */
public class EventTest extends MultiDriverTestClass {

    private Session session;
    Document a;
    Document b;
    Document c;
    Document d;
    Document e;
    Folder folder;

    Actor jim, bruce, lee, stan;
    Knows knowsJB;
    Knows knowsLS;
    Knows knowsJL;

    EventListenerTest eventListenerTest;

    @Before
    public void init() throws IOException {
        // each test should instantiate a new one
        eventListenerTest = null;
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.filesystem", "org.neo4j.ogm.domain.cineasts.annotated");
        session = sessionFactory.openSession();
        //session.purgeDatabase();
        a = new Document();
        a.setName("a");

        b = new Document();
        b.setName("b");

        c = new Document();
        c.setName("c");

        d = new Document();
        d.setName("d");

        e = new Document();
        e.setName("e");

        folder = new Folder();
        folder.setName("folder");

        folder.getDocuments().add(a);
        folder.getDocuments().add(b);
        folder.getDocuments().add(c);

        a.setFolder(folder);
        b.setFolder(folder);
        c.setFolder(folder);

        session.save(folder);
        session.save(d);
        session.save(e);

        jim = new Actor("Jim");
        bruce = new Actor("Bruce");
        lee = new Actor("Lee");
        stan = new Actor("Stan");

        knowsJB = new Knows();
        knowsJB.setFirstActor(jim);
        knowsJB.setSecondActor(bruce);
        knowsJB.setSince(new Date());

        jim.getKnows().add(knowsJB);
        session.save(jim);

        knowsLS = new Knows();
        knowsLS.setFirstActor(lee);
        knowsLS.setSecondActor(stan);
        knowsLS.setSince(new Date());

        lee.getKnows().add(knowsLS);
        session.save(lee);

        knowsJL = new Knows();
        knowsJL.setFirstActor(jim);
        knowsJL.setSecondActor(lee);

        // made a logical error on this, somehow -- it made knowsJB non-existant
        lee.getKnows().add(knowsJL);
        session.save(lee);
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
        //testExpectedSequenceTargetObjectTypes(eventListenerTest, expectedObjectTypes);
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
        //testExpectedSequenceTargetObjectTypes(eventListenerTest, expectedObjectTypes);
    }

    @Test
    public void testAddMultipleNodesAsList() {

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
        //testExpectedSequenceTargetObjectTypes(eventListenerTest, expectedObjectTypes);
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
        //testExpectedSequenceTargetObjectTypes(eventListenerTest, expectedObjectTypes);
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
        //testExpectedSequenceTargetObjectTypes(eventListenerTest, expectedObjectTypes);
    }

    @Test
    public void testAddAndAlterMultipleConnectedNodes() {
        int expectedNumberOfEvents = 6;
        eventListenerTest = new EventListenerTest(expectedNumberOfEvents);
        session.register(eventListenerTest);

        // the node is connected, the connected nodes are dirty, so additional events should fire
        a.setName("newA");
        b.setName("newB");
        c.setName("newC");
        session.save(a);

        Class[] expectedObjectTypes = new Class[expectedNumberOfEvents];
        for(int i=0;i<expectedNumberOfEvents;i++)
            expectedObjectTypes[i] = Document.class;

        testExpectedNumberOfEventsInQueue(eventListenerTest,expectedNumberOfEvents);
        //testExpectedSequenceTargetObjectTypes(eventListenerTest, expectedObjectTypes);
    }

    @Test
    public void testAddAndAlterMultipleConnectedNode() {
        int noOfExpectedEvents = 12;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
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
        //testExpectedSequenceTargetObjectTypes(eventListenerTest, expectedObjectTypes);
    }

    @Test
    public void testAddOneNewRelationship() {
        int noOfExpectedEvents = 2;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        // shouldn't this fire events for both the Document and the Folder ?!?
        d.setFolder(folder);
        session.save(d);

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        //testExpectedSequenceTargetObjectTypes(eventListenerTest, expectedObjectTypes);
    }

    @Test
    public void testAddNewRelationships() {
        int noOfExpectedEvents = 4;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        d.setFolder(folder);
        session.save(d);

        e.setFolder(folder);
        session.save(e);

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
    }

    @Test
    public void testAddOneRelationshipEntity() {

    }

    @Test
    public void testAlterOneRelationshipEntity() {
        // when altering a relationship, the relationship is first deleted, and then added. every relationship altered triggers 2 events(TransientRelationship and RelationshipEntity)
        int noOfExpectedEvents = 4;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        // i need a random date here, otherwise it will not be counted as dirty
        Random r = new Random();
        knowsJB.setSince(new Date((long) (1293861599+ r.nextDouble()*60*60*24*365)));
        session.save(knowsJB);

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
    }

    @Test
    public void testAlterMultipleRelationshipEntitiesWhoseObjectsAreNotConnected() {
        int noOfExpectedEvents = 800;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        knowsJB.setSince(new Date(System.nanoTime()));
        session.save(knowsJB);

        knowsLS.setSince(new Date(System.nanoTime()));
        session.save(knowsLS);

        //testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
    }

    @Test
    public void testAlterMultipleRelationshipEntitiesWhoseObjectsAreConnected() {
        int noOfExpectedEvents = 4;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        knowsJL.setSince(new Date(System.nanoTime()));
        session.save(knowsJL);

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
    }

    @Test
    public void deleteOneUnconnectedNode() {
        int noOfExpectedEvents = 2;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        session.delete(d);

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
    }

    @Test
    public void deleteOneConnectedNode() {
        int noOfExpectedEvents = 2;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        session.delete(a);

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
    }

    // no events should fire
    @Test
    public void testDeleteUnpersistedEntity() {
        Document unpersistedDocument = new Document();
        int noOfExpectedEvents = 0;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);
        session.delete(unpersistedDocument);
        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
    }

    @Test
    public void deleteOneRelationship() {
        int noOfExpectedEvents = 2;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        session.delete(knowsJL);

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
    }

    @Test
    public void deleteMultipleRelationships() {
        int noOfExpectedEvents = 4;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        session.delete(knowsJL);
        session.delete(knowsJB);

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
    }

    // session.save(<? implements Collection>);
    @Test
    public void saveMultipleNewNodes() {
        int noOfExpectedEvents = 2;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        a.setName("newA");
        b.setName("newB");
        c.setName("newC");
        List<Object> saveList = new LinkedList<>();
        saveList.add(a);saveList.add(b);saveList.add(c);

        session.save(saveList);

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
    }

    @Test
    public void deleteMultipleNodes() {
        int noOfExpectedEvents = 6;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        List<Object> saveList = new LinkedList<>();
        saveList.add(a);saveList.add(b);saveList.add(c);

        session.delete(saveList);

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
    }

    @Test
    public void deleteAllOfType() {

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
            assertTrue(types[i].isInstance(eventListener.eventsCaptured[i].getTargetObjects()));
    }

    private void testCountOfExpectedTargetObjects(EventListenerTest eventListener, TargetObjectCount[] targetObjectCountArray) {

    }
    /*
    private void testCountOfExpectedTargetObjects(EventListenerTest eventListener, TargetObjectCount[] targetObjectCountArray) {
        Class currentClass = null;
        for(int i=0;i<targetObjectCountArray.length;i++) {
            assertContainsObjects(eventListener,targetObjectCountArray[i]);
        }
    }
    */
    /*
    private void assertContainsObjects(EventListenerTest eventListener, TargetObjectCount targetObjectCountArray) {
        int numberOfObjectsOfTypeFound = 0;
        for(int i=0;i<eventListener.eventsCaptured.length;i++) {
            if(targetObjectCountArray.targetObjectType.isInstance(eventListener.eventsCaptured[i].getTargetObject()))
                numberOfObjectsOfTypeFound++;
        }
        assertTrue(numberOfObjectsOfTypeFound == targetObjectCountArray.count);
    }
    */
    class TargetObjectCount {
        public Class targetObjectType;
        public int count;
    }

    @After
    public void clean() throws IOException {
        session.purgeDatabase();
    }

}
