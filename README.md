# 🔍 Search Nucleus Lucene 101 🔍

## A Simple Spring Service  for learning and working with Lucene

![image info](lucene-you-got-some-searchin-to-do.jpeg)

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
There is a WEB-Endpoint which can be used for doing simple searches on the mini Index:

````localhost:8080/api/v1/search/query````
with request parameter ````query_term````

example: ````http://localhost:8080/api/v1/search/query?query_term=pullover````

## Tasks
There exists a ```trialday``` branch for the asignees where the corresponding solution (including the tests) has been removed.
### Search Task
Currently the result for the query term "blaue strickjacke" leaves alot to be desired:
Alot of Products that contain this property are not selected and returned leading to a [recall](https://en.wikipedia.org/wiki/Precision_and_recall#Recall)
For example this [productvariation](https://www.otto.de/p/vila-strickjacke-viril-1-tlg-S082R048/#variationId=S082R048J552).        
The mentioned product has a field **baseColor** which has the value *blau* which should match based on our query term.      

##### Goal:
* Make the product mentioned above occur in the search result with the query term "blaue strickjacke"

##### How to solve the task
* Expand the regular expression in the `Indexer` so that the **baseColor** field is indexed
* Switch out the standard Lucene ```QueryParser``` for the ``MultiFieldQueryParser`` and incorporate the **baseColor**
* The default token operator should be set to AND
* The solution must be tested

##### Keypoints / Challenges
* Lucene knowledge concerning the MultiFieldQueryParser
* Implementation of a Spring integration test
* Navigating through a new code base