Neo4j OGM - An Object Graph Mapping Library for Neo4j
===============

Neo4j OGM is a fast object-graph mapping library for Neo4j, optimised for server-based installations and utilising Cypher via the transactional HTTP endpoint.

It aims to simplify development with the Neo4j graph database and like JPA, it uses annotations on simple POJO domain objects.
Together with metadata, the annotations drive mapping the POJO entities and their fields to nodes, relationships, and properties in the graph database.

##Quick start

### Dependencies for Neo4j OGM

#### Maven

```xml
<dependency>
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j-ogm-core</artifactId>
    <version>{version}</version>
</dependency>

<dependency> <!-- If you're using the HTTP driver -->
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j-ogm-http-driver</artifactId>
    <version>{version}</version>
</dependency>

<dependency> <!-- If you're using the Embedded driver -->
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j-ogm-embedded-driver</artifactId>
    <version>{version}</version>
</dependency>
```

#### Gradle

```xml
dependencies {
    compile 'org.neo4j:neo4j-ogm-core:{version}'
    compile 'org.neo4j:neo4j-ogm-http-driver:{version}'
    compile 'org.neo4j:neo4j-ogm-embedded-driver:{version}'
}
```

#### Ivy

```xml
<dependency org="org.neo4j" name="neo4j-ogm-core" rev="{version}"/>
<dependency org="org.neo4j" name="neo4j-ogm-http-driver" rev="{version}"/>
<dependency org="org.neo4j" name="neo4j-ogm-embedded-driver" rev="{version}"/>
```

### Set up domain entities

```java

@NodeEntity
public class Actor {

	@GraphId
	private Long id;
	private String name;

	@Relationship(type = "ACTS_IN", direction = "OUTGOING")
	private Set<Movie> movies = new HashSet<>();

	public Actor() {
	}

	public Actor(String name) {
		this.name = name;
	}

	public void actsIn(Movie movie) {
		movies.add(movie);
		movie.getActors().add(this);
	}
}


@NodeEntity
public class Movie {

	@GraphId
	private Long id;
	private String title;
	private int released;

	@Relationship(type = "ACTS_IN", direction = "INCOMING")
	Set<Actor> actors;

	public Movie() {
	}

	public Movie(String title, int year) {
		this.title = title;
		this.released = year;
	}

}


```

### Configuration
The OGM can be configured in two ways. The easiest is auto configuration, where `ogm.properties` must be on the classpath.
The other is via Java configuration.
Please see examples [here](http://neo4j.com/docs/ogm/java/stable/)


### Persist/Load entities

```java


//Set up the Session
SessionFactory sessionFactory = new SessionFactory("movies.domain");
Session session = sessionFactory.openSession();

Movie movie = new Movie("The Matrix", 1999);

Actor keanu = new Actor("Keanu Reeves");
keanu.actsIn(movie);

Actor carrie = new Actor("Carrie-Ann Moss");
carrie.actsIn(movie);

//Persist the movie. This persists the actors as well.
session.save(movie);


//Load a movie
Movie matrix = session.load(Movie.class, movie.getId());
for(Actor actor : matrix.getActors()) {
    System.out.println("Actor: " + actor.getName());
}

```

## Getting Help

The [reference guide](http://neo4j.com/docs/ogm/java/stable/) is the best place to get started.

[Neo4j-OGM University](https://github.com/neo4j-examples/neo4j-ogm-university/tree/2.0), the sample application from the reference guide is a working example of a Spring Boot app that uses the Neo4j OGM library.
A version that uses the Embedded driver is [also available](https://github.com/neo4j-examples/neo4j-ogm-university/tree/2.0-embedded).

## Snapshots

To use the latest development version, just clone this repository and run `mvn clean install`
