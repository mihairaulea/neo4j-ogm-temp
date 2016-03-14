package org.neo4j.ogm.persistence.session.events;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;

/**
 * Created by Mihai Raulea on 3/14/2016.
 */
public class EventTest {

    private Session session;
    Document a;
    Document b;
    Document c;
    Folder f;

    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.filesystem");
        session = sessionFactory.openSession();

        a = new Document();
        a.setName("a");

        b = new Document();
        b.setName("b");

        c = new Document();
        c.setName("c");

        f = new Folder();
        f.setName("f");

        f.getDocuments().add(a);
        f.getDocuments().add(b);

        a.setFolder(f);
        b.setFolder(f);

        session.save(f);
        //session.clear();
    }

    @Test
    public void testSaveCallback() {
        a.setName("newA");
        b.setName("newB");
        c.setName("newC");
        f.setName("newF");
        session.save(f);
        Document aa = session.load(Document.class, a.getId());
        Document bb = session.load(Document.class, b.getId());
        System.out.println(aa.getName());
        System.out.println(bb.getName());
    }

}
