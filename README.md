These are the sample Bounded Contexts from the book
"Implementing Domain-Driven Design" by Vaughn Vernon:

http://vaughnvernon.co/?page_id=168

The models and surrounding architectural mechanisms
may be in various states of flux as the are refined
over time. Some tests may be incomplete. The code is
not meant to be a reflection of a production quality
work, but rather as a set of reference projects for
the book.

Points of Interest
==================

The iddd_agilepm project uses a key-value store as
its underlying persistence mechanism, and in particular
is LevelDB. Actually the LevelDB in use is a pure Java
implementation: https://github.com/dain/leveldb

Currently iddd_agilepm doesn't employ a container of
any kind (such as Spring).

The iddd_collaboration project uses Event Sourcing and
CQRS. It purposely avoids the use of an object-relational
mapper, showing that a simple JDBC-based query engine
and DTO matter can be used instead. This technique does
have its limitations, but it is meant to be small and fast
and require no configuration or annotations. It is not
meant to be perfect.

It may be helpful to make one additional mental note on
the iddd_collaboration CQRS implementation. To keep the
example simple it persists the Event Sourced write model
and the CQRS read model in one thread. Since two different
stores are used--LevelDB for the Event Journal and MySQL
for the read model--there may be very slight opportunities
for inconsistency, but not much. The idea was to keep the
two models as close to consistent as possible without
using the same data storage (and transaction) for both.
Two different storage mechanisms were used purposely to
demonstrate that they can be separate.

The iddd_identityaccess project uses object-relational
mapping (Hibernate), but so as not to leave it "boring" it
provides a RESTful client interface and even publishes
Domain-Event notifications via REST (logs) and RabbitMQ.

Finally the iddd_common project provides a number of reusable
components. This is not an attempt to be a framework, but
just leverages reuse to the degree that code copying doesn't
liter each project. This is not a recommendation, but it
did work well and save a considerable amount of work while
producing the samples.

Usage
=====

Requires
--------

- Java 25 (the Gradle toolchain is pinned to 25; `make` finds it via `/usr/libexec/java_home -v 25`)
- Docker (with the `docker compose` plugin) for MySQL and RabbitMQ

Setup (with Docker)
-------------------

```
make up          # = docker compose up -d --wait
```

`compose.yaml` starts two containers; `--wait` blocks until both report healthy:

- `iddd-mysql` — `mysql:9` on `localhost:3306`, root password `root`. The four SQL scripts
  (`test_common.sql`, `common.sql`, `iam.sql`, `collaboration.sql`) are mounted into
  `/docker-entrypoint-initdb.d/` and run on the first start, creating `iddd_common_test`,
  `iddd_iam` and `iddd_collaboration`.
- `iddd-rabbitmq` — `rabbitmq:4-management` on `localhost:5672`; management UI on
  http://localhost:15672 (guest/guest). The code connects with the broker defaults.

`make down` removes the containers. No volumes are declared, so the next `make up` starts
from an empty data directory and re-runs the init scripts — that is the intended way to
reset the databases.

Build
------

```
./gradlew build      # or: make build   (make test = ./gradlew test --continue)
```

This downloads Gradle 9.6 via the wrapper and builds all four projects, including running
the tests, which need the two containers above (they connect to `localhost` with the
default credentials; the identityaccess resource tests additionally bind an embedded
Undertow to port 8081).

The Gradle build using Maven repositories was provided by
Michael Andrews (Github michaelajr and Twitter @MichaelAJr).
Thanks much!

Modernization notes (2026)
==========================

The samples were written for Java 7 and did not build on anything newer. This tree is based
on the `foenye/IDDD_Samples` fork, whose commit
"refactor(build): upgrade gradle 9.6.0 + java25 + spring boot 4.1.0 dependencies" did the
bulk of the upgrade; the commits on top of it fix the remaining test failures, replace the
infra scripts with docker compose and tidy the build. What changed versus upstream:

- Java 7 → Java 25 (Gradle toolchain), Gradle 2.3 → 9.6.0 (wrapper), `compile` → `api`/`implementation`.
- Dependency versions come from the Spring Boot 4.1.0 BOM (`spring-boot-dependencies`) —
  only the BOM is used, not Spring Boot itself: the contexts are still plain
  `ClassPathXmlApplicationContext` + XML bean definitions.
- Spring 2.5.6 → Spring Framework 7.0 (un-versioned XSD locations; the `autowire="byName"`
  attributes on the in-memory test repositories, which take no constructor args, were dropped).
- Hibernate 3.2.7 → Hibernate ORM 7.4; the `*.hbm.xml` mappings are still used as-is.
- javax.* → jakarta.* (`ws.rs`, `persistence`, `transaction`, `servlet`); only JDK `javax.sql` remains.
- RESTEasy 2.0.1 + TJWS → RESTEasy 7.0.2 + embedded Undertow (`UndertowJaxrsServer`) for the
  identityaccess resource tests; the JAX-RS client tests use the standard `jakarta.ws.rs.client` API.
- LevelDB (dain/leveldb, pure Java) 0.5 → 0.12. It, and the Guava 21 it pulls in, still use
  `sun.misc.Unsafe`, so Java 25 prints `sun.misc.Unsafe::invokeCleaner` /
  `sun.misc.Unsafe::objectFieldOffset ... will be removed in a future release` warnings during
  the agilepm and collaboration tests — harmless today, but a future JDK will break it unless
  the library is replaced.
- amqp-client 3.0.4 → 5.x; connection settings are the broker defaults (`guest`/`guest`, vhost `/`)
  as in upstream — the fork briefly hard-coded `rabbit`/`rabbit` for a Bitnami image, which is reverted.
- MySQL Connector/J 5.1 → `com.mysql:mysql-connector-j` 9.x against a MySQL 9 server;
  commons-dbcp 1.4 → commons-dbcp2.
- `DomainRegistry`, `ApplicationServiceRegistry` and `ApplicationContextProvider` no longer
  guard `if (applicationContext == null)`: each test builds a fresh context and the static must
  follow it, otherwise later tests resolve beans from an already-closed context.
- `startContainers.sh` and the `db_setup.sh`/`.bat` scripts are gone; `compose.yaml` + `Makefile`
  replace them.
- Tests are still JUnit 4 (`junit.framework.TestCase`), run with Gradle's `useJUnit()`.

Things to know:

- `mavenLocal()` is still first in the repository list (upstream's choice); a stale `~/.m2` can
  shadow Maven Central.
- The tests are integration tests against the real containers and share the databases, so run
  one Gradle invocation at a time.


I hope you benefit from the samples.

Vaughn Vernon
Author: Implementing Domain-Driven Design
Twitter: @VaughnVernon
http://vaughnvernon.co/
