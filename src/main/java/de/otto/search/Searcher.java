package de.otto.search;

import edu.wisc.ischool.wiscir.examples.BM25SimilarityOriginal;
import edu.wisc.ischool.wiscir.utils.LuceneUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class Searcher {

    private final PropertyConfiguration propertyConfiguration;


    public SearchDocument getByDocNo(final String variationId) throws ParseException, IOException {

        String pathIndex = propertyConfiguration.getIndexPath();

        QueryParser parser = new QueryParser(FieldDefinition.DOCNO.getFieldName(), new KeywordAnalyzer());
        // a query parser that transforms a text string into Lucene's query object

        Query query = parser.parse(variationId); // this is Lucene's query object

        // Okay, now let's open an index and search for documents
        Directory dir = FSDirectory.open(new File(pathIndex).toPath());
        IndexReader index = DirectoryReader.open(dir);

        // you need to create a Lucene searcher
        IndexSearcher searcher = new IndexSearcher(index);

        final TopDocs docs = searcher.search(query, 1);

        assert docs.scoreDocs.length == 1;

        final ScoreDoc scoreDoc = docs.scoreDocs[0];
        final int docid = scoreDoc.doc;

        String pbk = LuceneUtils.getDocField(index, FieldDefinition.PRODUKT_BASIS_KLASSE.getFieldName(), docid);
        String title = LuceneUtils.getDocField(index, FieldDefinition.TITLE.getFieldName(), docid);
        String color = LuceneUtils.getDocField(index, FieldDefinition.COLOR.getFieldName(), docid);

        return SearchDocument.of(docid, variationId, title, pbk, 1, 1.0, color);
    }

    public SearchResult retrieve(String queryTerm) throws ParseException, IOException {
        String pathIndex = propertyConfiguration.getIndexPath();

        MultiFieldQueryParser parser = new MultiFieldQueryParser(
                new String[]{
                        FieldDefinition.TITLE.getFieldName(),
                        FieldDefinition.COLOR.getFieldName()
                },
                new SimpleGermanAnalyzer()); // a query parser that transforms a text string into Lucene's query object

        parser.setDefaultOperator(QueryParser.Operator.AND);
        Query query = parser.parse(queryTerm); // this is Lucene's query object

        // Okay, now let's open an index and search for documents
        Directory dir = FSDirectory.open(new File(pathIndex).toPath());
        IndexReader index = DirectoryReader.open(dir);

        // you need to create a Lucene searcher
        IndexSearcher searcher = new IndexSearcher(index);
        query.createWeight(searcher, ScoreMode.TOP_DOCS, 3f);

        // make sure the similarity class you are using is consistent with those being used for indexing
        searcher.setSimilarity(new BM25SimilarityOriginal());

        int top = 100; // Let's just retrieve the top 100 results
        TopDocs docs = searcher.search(query, top); // retrieve the top 100 results; retrieved results are stored in TopDocs

        final SearchResult searchResult = SearchResult.of(queryTerm, docs.totalHits.value);
        int rank = 1;
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            int docid = scoreDoc.doc;
            double score = scoreDoc.score;
            String docno = LuceneUtils.getDocField(index, FieldDefinition.DOCNO.getFieldName(), docid);
            String pbk = LuceneUtils.getDocField(index, FieldDefinition.PRODUKT_BASIS_KLASSE.getFieldName(), docid);
            String title = LuceneUtils.getDocField(index, FieldDefinition.TITLE.getFieldName(), docid);
            String color = LuceneUtils.getDocField(index, FieldDefinition.COLOR.getFieldName(), docid);

            final SearchDocument document = SearchDocument.of(docid, docno, title, pbk, rank, score, color);
            searchResult.addDocument(document);
            rank++;
        }

        // remember to close the index and the directory
        index.close();
        dir.close();

        return searchResult;
    }

}
