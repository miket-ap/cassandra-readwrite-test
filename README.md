cassandra-readwrite-test
========================

Cassandra read-write performance test using the astyanax library

Configuration
-------------
The app will look for the app.properties file in the current directory. So update it to match your cassandra environment.

Running it
----------
The project builds a fully executable .jar file with all the dependencies included. 

Usage (from the project root folder):
```no-highlight
java -jar target/cassandra-read-write-test-0.0.1-SNAPSHOT-SHADED.jar <page_size> <number_of_threads>
```

A page size of 100 and 10 threads are good numbers to start with. You may also want to increase the maximum number of connections per host  in the app.properties file or decrease the number of threads if you start getting connection timeouts.
