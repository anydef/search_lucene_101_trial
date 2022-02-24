# This is a clone/fork from: https://github.com/jiepujiang/LuceneTutorial

## A Simple Tutorial of Lucene's Indexing and Search Systems

Outline:
* [Installation](https://github.com/otto-ec/search_lucene_101#installation)
* [Building an Index](https://github.com/otto-ec/search_lucene_101#build-an-index)
* [Working with an Index](https://github.com/otto-ec/search_lucene_101#working-with-an-index)
    * [External and Internal IDs](https://github.com/otto-ec/search_lucene_101#external-and-internal-ids)
    * [Frequency Posting List](https://github.com/otto-ec/search_lucene_101#frequency-posting-list)
    * [Position Posting List](https://github.com/otto-ec/search_lucene_101#position-posting-list)
    * [Accessing an Indexed Document](https://github.com/otto-ec/search_lucene_101#accessing-an-indexed-document)
    * [Document and Field Length](https://github.com/otto-ec/search_lucene_101#document-and-field-length)
    * [Iterate Through the Vocabulary](https://github.com/otto-ec/search_lucene_101#iterate-through-the-vocabulary)
    * [Corpus-level Statistics](https://github.com/otto-ec/search_lucene_101#corpus-level-statistics)
* [Searching](https://github.com/otto-ec/search_lucene_101#searching)

## Environment

This tutorial uses:
* Oracle JDK 11
* Lucene 8.10.1

## Installation

Apache Lucene is an open-source information retrieval toolkit written in Java.

The easiest way to use Lucene in your project is to import it using Maven.
You need to at least import ```lucene-core``` (just pasting the following to your ```pom.xml```'s dependencies).

```xml
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-core</artifactId>
    <version>8.10.1</version>
</dependency>
```

You may also need ```lucene-analyzers-common``` and ```lucene-queryparser```.

```xml
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-analyzers-common</artifactId>
    <version>8.10.1</version>
</dependency>
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-queryparser</artifactId>
    <version>8.10.1</version>
</dependency>
```

If you do not use Maven, you need to download the jar files by yourself and include them into your project.
Make sure you download the correct version.
http://archive.apache.org/dist/lucene/java/8.10.1/

Support:
* Official API documentation: http://lucene.apache.org/core/8_10_1/

## Build an Index

### Corpus

This tutorial uses a small excerpt corpus from our product data used in team Search-Nucleus as input for the Solr cluster.
You can download the corpus at https://github.com/otto-ec/search_lucene_101/blob/master/example_search_variation_otto.xml.gz

The corpus includes the information of about 900 variations of products.
Each variation (document) is in the following format:

```xml
<searchVariation xmlns="">
  <id>S0N150CJL9WJ</id>
  <title>Disney Baseball Cap »Disney - Marie - Satin Nylon Curved Bill Cap Snapback NEU COOL«</title>
    ...
  <produktBasisKlasse>Caps</produktBasisKlasse>
    ...
  <brand>
    <brandCode>DISNEY</brandCode>
    <brandName>Disney</brandName>
  </brand>
    ...
  <productTypes>
    <productType>Baseball Caps</productType>
  </productTypes>
</searchVariation>
```

A document has five fields.
The DOCNO field specifies a unique ID (docno) for each article.
We need to build an index for the other four text fields such that we can retrieve the documents.

### Text Processing and Indexing Options

Many IR systems may require you to specify a few text processing options for indexing and retrieval:
* **Tokenization** -- how to split a sequence of text into individual tokens (most tokens are just words).
* **Case-folding** -- Most IR systems ignore letter case differences. 
But sometimes letter case may be important, e.g., **smart** and **SMART** (the SMART retrieval system). 
* **Stop words** -- You may want to remove some stop words such as **is**, **the**, and **to**. 
Removing stop words can significantly reduce index size. 
But it may also cause problems for some search queries such as ```to be or not to be```.
We recommend you to keep them unless you cannot afford the disk space.
* **Stemming** -- Stemmers generate [word stems](https://en.wikipedia.org/wiki/Word_stem). You may want to index stemmed words rather than the original ones to ignore minor word differences such as **model** vs. **models**.
Stemming algorithms are not perfect and may get wrong. IR systems often use **Porter** or **Krovetz** stemming (Krovetz is more common for IR research and gives better results on most datasets based on my impression). 
Just a few examples for their differences:

Original    | Porter    | Krovetz
--------    | -------   | -------
relevance   | relev     | relevance
based       | base      | base
language    | languag   | language
models      | model     | model

An indexed document can have different fields to store different types of information. 
Most IR systems support two types of fields:
* **Metadata field** is similar to a structured database record's field. 
They are stored and indexed as a whole without tokenization.
It is suitable for data fields such as IDs (such as the docno field in our example corpus).
* **Normal text field** is suitable for regular text contents (such as the other four fields in our example corpus).
The texts are tokenized and indexed (using inverted index), such that you can search using normal text retrieval techniques. 

Some IR systems also support storing and indexing numeric values
(and you can search for indexed numeric values using range or greater-than/less-than queries) and other data types.

### Lucene Examples

This is an example program that uses Lucene to build an index for the example corpus. 
```java
// change the following input and output paths to your local ones
String pathCorpus = "example_search_variation_otto.xml.gz";
String pathIndex = "example_index_lucene";
Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );

// Analyzer specifies options for text tokenization and normalization (e.g., stemming, stop words removal, case-folding)
Analyzer analyzer = new Analyzer() {
@Override
protected TokenStreamComponents createComponents( String fieldName ) {
// Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
TokenStreamComponents ts = new TokenStreamComponents( new StandardTokenizer() );
// Step 2: transforming all tokens into lowercased ones (recommended for the majority of the problems)
ts = new TokenStreamComponents( ts.getSource(), new LowerCaseFilter( ts.getTokenStream() ) );
// Step 3: whether to remove stop words (unnecessary to remove stop words unless you can't afford the extra disk space)
// Uncomment the following line to remove stop words
ts = new TokenStreamComponents( ts.getSource(), new StopFilter( ts.getTokenStream(), GermanAnalyzer.getDefaultStopSet()) );
// Step 4: whether to apply stemming
// Uncomment one of the following two lines to apply Krovetz or Porter stemmer (Krovetz is more common for IR research)
ts = new TokenStreamComponents( ts.getSource(), new GermanLightStemFilter( ts.getTokenStream() ) );
//ts = new TokenStreamComponents( ts.getSource(), new GermanStemFilter( ts.getTokenStream() ) );
return ts;
}
};

IndexWriterConfig config = new IndexWriterConfig( analyzer );
// Note that IndexWriterConfig.OpenMode.CREATE will override the original index in the folder
config.setOpenMode( IndexWriterConfig.OpenMode.CREATE );
// Lucene's default BM25Similarity stores document field length using a "low-precision" method.
// Use the BM25SimilarityOriginal to store the original document length values in index.
config.setSimilarity( new BM25SimilarityOriginal() );

IndexWriter ixwriter = new IndexWriter( dir, config );

// This is the field setting for metadata field (no tokenization, searchable, and stored).
FieldType fieldTypeMetadata = new FieldType();
fieldTypeMetadata.setOmitNorms( true );
fieldTypeMetadata.setIndexOptions( IndexOptions.DOCS );
fieldTypeMetadata.setStored( true );
fieldTypeMetadata.setTokenized( false );
fieldTypeMetadata.freeze();

// This is the field setting for normal text field (tokenized, searchable, store document vectors)
FieldType fieldTypeText = new FieldType();
fieldTypeText.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS );
fieldTypeText.setStoreTermVectors( true );
fieldTypeText.setStoreTermVectorPositions( true );
fieldTypeText.setTokenized( true );
fieldTypeText.setStored( true );
fieldTypeText.freeze();

// You need to iteratively read each document from the example corpus file,
// create a Document object for the parsed document, and add that
// Document object by calling addDocument().

// Well, the following only works for small text files. DO NOT follow this part for large dataset files.
InputStream instream = new GZIPInputStream( new FileInputStream( pathCorpus ) );
String corpusText = new String( IOUtils.toByteArray( instream ), "UTF-8" );
instream.close();

Pattern pattern = Pattern.compile(
"<searchVariation.+?>" +
"<id>(.+?)</id>.+?" +
"<title>(.+?)</title>.+?" +
"<brandName>(.+?)</brandName>.+?" +
"<produktBasisKlasse>(.+?)</produktBasisKlasse>.+?" +
"<productType>(.+?)</productType>.+?" +
"</searchVariation>",
Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL
);

Matcher matcher = pattern.matcher( corpusText );

while ( matcher.find() ) {

String docno = matcher.group( 1 ).trim();
String title = matcher.group( 2 ).trim();
String brand = matcher.group( 3 ).trim();
String pbk = matcher.group( 4 ).trim();
String ptype = matcher.group( 5 ).trim();

// Create a Document object
Document d = new Document();
// Add each field to the document with the appropriate field type options
d.add( new Field( "docno", docno, fieldTypeMetadata ) );
d.add( new Field( "title", title, fieldTypeText ) );
d.add( new Field( "brand", brand, fieldTypeText ) );
d.add( new Field( "pbk", pbk, fieldTypeText ) );
d.add( new Field( "ptype", ptype, fieldTypeText ) );
// Add the document to the index
System.out.println( "indexing document " + docno );
ixwriter.addDocument( d );
}

System.out.println( "finished adding docs ...");
ixwriter.commit();

// remember to close both the index writer and the directory
ixwriter.close();
dir.close();
```

You can download the Lucene index for the example corpus at https://github.com/otto-ec/search_lucene_101/blob/master/example_index_lucene.tar.gz

## Working with an Index

### Openning and Closing an Index

Lucene uses the IndexReader class to operate all index files.

```java
// modify to your index path
String pathIndex = "index_example_lucene"; 

// First, open the directory
Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );

// Then, open an IndexReader to access your index
IndexReader index = DirectoryReader.open( dir );

// Now, start working with your index using the IndexReader object

index.numDocs(); // just an example: get the number of documents in the index

// Remember to close both the IndexReader and the Directory after use 
index.close();
dir.close();
``` 

### External and Internal IDs

In IR experiments, we often use some unique IDs to identify documents in the corpus. 
For example, our example corpus (and most TREC corpora) uses docno as the unique identifer.

However, IR systems often use some internal IDs to identify the indexed documents.
These IDs are often subject to change and not transparent to the users.
So you often need to transform between external and internal IDs when locating documents in an index.

To help you get started quickly, we provide
a utility class ```edu.wisc.ischool.wiscir.utils.LuceneUtils``` to help you transform between 
the two IDs.
```java
IndexReader index = DirectoryReader.open( dir );

// the name of the field storing external IDs (docnos)
String fieldName = "docno";

int docid = 5;
LuceneUtils.getDocno( index, fieldName, docid ); // get the docno for the internal docid = 5

String docno = "ACM-1835461";
LuceneUtils.findByDocno( index, fieldName, docno ); // get the internal docid for docno "ACM-1835461"
```

### Frequency Posting List

You can retrieve a term's posting list from an index.
The simplest form is document-frequency posting list,
where each entry in the list is a ```<docid,frequency>``` pair (only includes the documents containing that term).
The entries are sorted by docids such that you can efficiently compare and merge multiple lists.

The following program retrieves the posting list for a term ```reformulation``` in the ```text``` field from the Lucene index:
```java
String pathIndex = "example_index_lucene";

// Let's just retrieve the posting list for the term "reformulation" in the "text" field
String field = "title";
String term = "poster";

Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
IndexReader index = DirectoryReader.open( dir );

// The following line reads the posting list of the term in a specific index field.
// You need to encode the term into a BytesRef object,
// which is the internal representation of a term used by Lucene.
System.out.printf( "%-10s%-15s%-6s\n", "DOCID", "DOCNO", "FREQ" );
PostingsEnum posting = MultiTerms.getTermPostingsEnum( index, field, new BytesRef( term ), PostingsEnum.FREQS );
if ( posting != null ) { // if the term does not appear in any document, the posting object may be null
int docid;
// Each time you call posting.nextDoc(), it moves the cursor of the posting list to the next position
// and returns the docid of the current entry (document). Note that this is an internal Lucene docid.
// It returns PostingsEnum.NO_MORE_DOCS if you have reached the end of the posting list.
while ( ( docid = posting.nextDoc() ) != PostingsEnum.NO_MORE_DOCS ) {
String docno = LuceneUtils.getDocno( index, "docno", docid );
int freq = posting.freq(); // get the frequency of the term in the current document
System.out.printf( "%-10d%-15s%-6d\n", docid, docno, freq );
}
}

index.close();
dir.close();
```

Note that the internal docids are subject to change and 
are often different between different systems and indexes. 

### Position Posting List

You can also retrieve a posting list with term postions in each document.

```java
String pathIndex = "example_index_lucene";

// Let's just retrieve the posting list for the term "reformulation" in the "text" field
String field = "title";
String term = "gardine";

Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
IndexReader index = DirectoryReader.open( dir );

// we also print out external ID
Set<String> fieldset = new HashSet<>();
fieldset.add( "docno" );

// The following line reads the posting list of the term in a specific index field.
// You need to encode the term into a BytesRef object,
// which is the internal representation of a term used by Lucene.
System.out.printf( "%-10s%-15s%-10s%-20s\n", "DOCID", "DOCNO", "FREQ", "POSITIONS" );
PostingsEnum posting = MultiTerms.getTermPostingsEnum( index, field, new BytesRef( term ), PostingsEnum.POSITIONS );
if ( posting != null ) { // if the term does not appear in any document, the posting object may be null
int docid;
// Each time you call posting.nextDoc(), it moves the cursor of the posting list to the next position
// and returns the docid of the current entry (document). Note that this is an internal Lucene docid.
// It returns PostingsEnum.NO_MORE_DOCS if you have reached the end of the posting list.
while ( ( docid = posting.nextDoc() ) != PostingsEnum.NO_MORE_DOCS ) {
String docno = index.document( docid, fieldset ).get( "docno" );
int freq = posting.freq(); // get the frequency of the term in the current document
System.out.printf( "%-10d%-15s%-10d", docid, docno, freq );
for ( int i = 0; i < freq; i++ ) {
// Get the next occurrence position of the term in the current document.
// Note that you need to make sure by yourself that you at most call this function freq() times.
System.out.print( ( i > 0 ? "," : "" ) + posting.nextPosition() );
}
System.out.println();
}
}

index.close();
dir.close();
```

### Accessing An Indexed Document

You can access an indexed document from an index. 
An index document is usually stored as a document vector, 
which is a list of <word,frequency> pairs.

```java
String pathIndex = "example_index_lucene";

// let's just retrieve the document vector (only the "text" field) for the Document with internal ID=21
String field = "title";
int docid = 21;

Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
IndexReader index = DirectoryReader.open( dir );

Terms vector = index.getTermVector( docid, field ); // Read the document's document vector.

// You need to use TermsEnum to iterate each entry of the document vector (in alphabetical order).
System.out.printf( "%-20s%-10s%-20s\n", "TERM", "FREQ", "POSITIONS" );
TermsEnum terms = vector.iterator();
PostingsEnum positions = null;
BytesRef term;
while ( ( term = terms.next() ) != null ) {
    
    String termstr = term.utf8ToString(); // Get the text string of the term.
    long freq = terms.totalTermFreq(); // Get the frequency of the term in the document.
    
    System.out.printf( "%-20s%-10d", termstr, freq );
    
    // Lucene's document vector can also provide the position of the terms
    // (in case you stored these information in the index).
    // Here you are getting a PostingsEnum that includes only one document entry, i.e., the current document.
    positions = terms.postings( positions, PostingsEnum.POSITIONS );
    positions.nextDoc(); // you still need to move the cursor
    // now accessing the occurrence position of the terms by iteratively calling nextPosition()
    for ( int i = 0; i < freq; i++ ) {
        System.out.print( ( i > 0 ? "," : "" ) + positions.nextPosition() );
    }
    System.out.println();
}

index.close();
dir.close();

```

### Document and Field Length

By default, Lucene stores some low-precision values of field length as a form of "document norms" (see Lucene's ```org.apache.lucene.search.similarities.BM25Similarity.computeNorm(FieldInvertState state)```  for details). 
You can implement a customized Similarity class to store the full-precision document field length values.
We have provided an example at ```edu.wisc.ischool.wiscir.examples.BM25SimilarityOriginal```. 
You will be able to access document length at search time (see ```edu.wisc.ischool.wiscir.examples.BM25SimilarityOriginal.BM25Scorer```).

You may also compute the document field length on your own if you have stored document vectors at indexing time.
The following program prints out the length of text field for each document in the example corpus, 
which also helps you understand how to work with a stored document vector:
```java
String pathIndex = "example_index_lucene";
String field = "title";

Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
IndexReader ixreader = DirectoryReader.open( dir );

// we also print out external ID
Set<String> fieldset = new HashSet<>();
fieldset.add( "docno" );

// The following loop iteratively print the lengths of the documents in the index.
System.out.printf( "%-10s%-15s%-10s\n", "DOCID", "DOCNO", "Length" );
for ( int docid = 0; docid < ixreader.maxDoc(); docid++ ) {
    String docno = ixreader.document( docid, fieldset ).get( "docno" );
    int doclen = 0;
    TermsEnum termsEnum = ixreader.getTermVector( docid, field ).iterator();
    while ( termsEnum.next() != null ) {
        doclen += termsEnum.totalTermFreq();
    }
    System.out.printf( "%-10d%-15s%-10d\n", docid, docno, doclen );
}

ixreader.close();
dir.close();
```

### Iterate Through the Vocabulary 

The following program iterates through the vocabulary and print out the first 100 words in the 
vocabulary and some word statistics.
```java
String pathIndex = "example_index_lucene";

// Let's just retrieve the vocabulary of the "text" field
String field = "title";

Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
IndexReader index = DirectoryReader.open( dir );

double N = index.numDocs();
double corpusLength = index.getSumTotalTermFreq( field );

System.out.printf( "%-30s%-10s%-10s%-10s%-10s\n", "TERM", "DF", "TOTAL_TF", "IDF", "p(w|c)" );

// Get the vocabulary of the index.

Terms voc = MultiTerms.getTerms( index, field );
// You need to use TermsEnum to iterate each entry of the vocabulary.
TermsEnum termsEnum = voc.iterator();
BytesRef term;
int count = 0;
while ( ( term = termsEnum.next() ) != null ) {
count++;
String termstr = term.utf8ToString(); // get the text string of the term
int n = termsEnum.docFreq(); // get the document frequency (DF) of the term
long freq = termsEnum.totalTermFreq(); // get the total frequency of the term
double idf = Math.log( ( N + 1.0 ) / ( n + 1.0 ) ); // well, we normalize N and n by adding 1 to avoid n = 0
double pwc = freq / corpusLength;
if (pwc > 0.001 && termstr.length() > 1)
System.out.printf( "%-30s%-10d%-10d%-10.2f%-10.8f\n", termstr, n, freq, idf, pwc );
if ( count >= 10000 ) {
break;
}
}

index.close();
dir.close();
```

### Corpus-level Statistics

```IndexReader``` provides many corpus-level statistics.
The follow program computes the IDF and corpus probability for the term ```reformulation```.
```java
String pathIndex = "example_index_lucene";

// Let's just count the IDF and P(w|corpus) for the word "reformulation" in the "text" field
String field = "title";
String term = "gardine";

Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
IndexReader index = DirectoryReader.open( dir );

int N = index.numDocs(); // the total number of documents in the index
int n = index.docFreq( new Term( field, term ) ); // get the document frequency of the term in the "text" field
double idf = Math.log( ( N + 1.0 ) / ( n + 1.0 ) ); // well, we normalize N and n by adding 1 to avoid n = 0

System.out.printf( "%-30sN=%-10dn=%-10dIDF=%-8.2f\n", term, N, n, idf );

long corpusTF = index.totalTermFreq( new Term( field, term ) ); // get the total frequency of the term in the "text" field
long corpusLength = index.getSumTotalTermFreq( field ); // get the total length of the "text" field
double pwc = 1.0 * corpusTF / corpusLength;

System.out.printf( "%-30slen(corpus)=%-10dfreq(%s)=%-10dP(%s|corpus)=%-10.6f\n", term, corpusLength, term, corpusTF, term, pwc );

// remember to close the index and the directory
index.close();
dir.close();
```

## Searching

The following program retrieves the top 10 articles for the query "query reformulation" 
from the example corpus using the BM25 search model. Note that we used the provided ```BM25SimilarityOriginal``` class for search because we built the example index using this class.
If you built your index based on Lucene's default ```BM25Similarity```, you should use the default ```BM25Similarity``` for BM25 search.  

```java
String pathIndex = "example_index_lucene";

// Analyzer specifies options for text tokenization and normalization (e.g., stemming, stop words removal, case-folding)
Analyzer analyzer = new Analyzer() {
@Override
protected TokenStreamComponents createComponents( String fieldName ) {
// Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
TokenStreamComponents ts = new TokenStreamComponents( new StandardTokenizer() );
// Step 2: transforming all tokens into lowercased ones (recommended for the majority of the problems)
ts = new TokenStreamComponents( ts.getSource(), new LowerCaseFilter( ts.getTokenStream() ) );
// Step 3: whether to remove stop words (unnecessary to remove stop words unless you can't afford the extra disk space)
// Uncomment the following line to remove stop words
ts = new TokenStreamComponents( ts.getSource(), new StopFilter( ts.getTokenStream(), GermanAnalyzer.getDefaultStopSet()) );
// Step 4: whether to apply stemming
// Uncomment one of the following two lines to apply Krovetz or Porter stemmer (Krovetz is more common for IR research)
ts = new TokenStreamComponents( ts.getSource(), new GermanLightStemFilter( ts.getTokenStream() ) );
//ts = new TokenStreamComponents( ts.getSource(), new GermanStemFilter( ts.getTokenStream() ) );
return ts;
}
};

String field = "title"; // the field you hope to search for
QueryParser parser = new QueryParser( field, analyzer ); // a query parser that transforms a text string into Lucene's query object

String qstr = "gold"; // this is the textual search query
Query query = parser.parse( qstr ); // this is Lucene's query object

// Okay, now let's open an index and search for documents
Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
IndexReader index = DirectoryReader.open( dir );

// you need to create a Lucene searcher
IndexSearcher searcher = new IndexSearcher( index );

// make sure the similarity class you are using is consistent with those being used for indexing
searcher.setSimilarity( new BM25SimilarityOriginal() );

int top = 100; // Let's just retrieve the talk 10 results
TopDocs docs = searcher.search( query, top ); // retrieve the top 10 results; retrieved results are stored in TopDocs

System.out.printf( "%-10s%-20s%-10s%-40s%s\n", "Rank", "DocNo", "Score", "PBK", "Title" );
int rank = 1;
for ( ScoreDoc scoreDoc : docs.scoreDocs ) {
int docid = scoreDoc.doc;
double score = scoreDoc.score;
String docno = LuceneUtils.getDocno( index, "docno", docid );
String pbk = LuceneUtils.getDocno( index, "pbk", docid );
String title = LuceneUtils.getDocno( index, "title", docid );
System.out.printf( "%-10d%-20s%-10.4f%-40s%s\n", rank, docno, score, pbk, title );
rank++;
}

// remember to close the index and the directory
index.close();
dir.close();
```
