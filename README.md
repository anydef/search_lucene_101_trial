# 🔍 Search Nucleus Lucene 101 🔍

## A Simple Spring Service  for learning and working with Lucene

Outline:
* [Prerequisites](#Prerequisites)
* [Run](#Run)
* [Tasks](#Tasks)

This application is used for search newbies and trial-day applicants. 

## Prerequisites

This application requires:
* Oracle JDK 11
* Maven

## Run

compile with:   
```mvn clean package```

run with:   
````mvn exec:java````

or alternatively you can start the application like any other SpringApp in your preferred IDE.

## Usage

After startup the app is available under ``localhost:8080``It takes a small subset of OTTO product variations and builds a local index on startup.
There is a WEB-Endpoint whith can be used for doing simple searches on the mini Index:

````localhost:8080/api/v1/retrieve````
with request parameter ````query_term````

example: ````http://localhost:8080/api/v1/search/retrieve?query_term=pullover````

## Tasks

### Java Lucene Task (For Juniors)
TODO setup basic project for programming tasks
what prior knowledge is required if any?
how to setup the task?
describe here what needs to be done
how to solve the task:
challenges / key points:
understanding of search concepts as seen in Search / Solr / Lucene Experience
write unit tests

### Java Lucene Task (For Seniors)
TODO setup basic project for programming tasks
what prior knowledge is required if any?
how to setup the task?
describe here what needs to be done
how to solve the task:
challenges / key points:
understanding of search concepts as seen in Search / Solr / Lucene Experience
write unit tests

