package de.otto.search;

import edu.wisc.ischool.wiscir.examples.BM25SimilarityOriginal;
import edu.wisc.ischool.wiscir.utils.LuceneUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
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


    public SearchDocument getByVariationId(final String variationId) throws ParseException, IOException {

        String pathIndex = propertyConfiguration.getIndexPath();

        QueryParser parser = new QueryParser("docno", new KeywordAnalyzer());
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

        String pbk = LuceneUtils.getDocno(index, "pbk", docid);
        String title = LuceneUtils.getDocno(index, "title", docid);

        return SearchDocument.of(docid, variationId, title, pbk, 1, 1.0);
    }

    public SearchResult retrieveByTitle(String queryTerm) throws ParseException, IOException {
        String pathIndex = propertyConfiguration.getIndexPath();

        QueryParser parser = new QueryParser("title", new SimpleGermanAnalyzer()); // a query parser that transforms a text string into Lucene's query object

        Query query = parser.parse(queryTerm); // this is Lucene's query object

        // Okay, now let's open an index and search for documents
        Directory dir = FSDirectory.open(new File(pathIndex).toPath());
        IndexReader index = DirectoryReader.open(dir);

        // you need to create a Lucene searcher
        IndexSearcher searcher = new IndexSearcher(index);

        // make sure the similarity class you are using is consistent with those being used for indexing
        searcher.setSimilarity(new BM25SimilarityOriginal());

        int top = 100; // Let's just retrieve the talk 10 results
        TopDocs docs = searcher.search(query, top); // retrieve the top 10 results; retrieved results are stored in TopDocs

        final SearchResult searchResult = SearchResult.of(queryTerm, docs.totalHits.value);
        log.info(String.format("%-10s%-20s%-10s%-40s%s\n", "Rank", "DocNo", "Score", "PBK", "Title"));
        int rank = 1;
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            int docid = scoreDoc.doc;
            double score = scoreDoc.score;
            String docno = LuceneUtils.getDocno(index, "docno", docid);
            String pbk = LuceneUtils.getDocno(index, "pbk", docid);
            String title = LuceneUtils.getDocno(index, "title", docid);
            log.info(String.format("%-10d%-20s%-10.4f%-40s%s\n", rank, docno, score, pbk, title));

            final SearchDocument document = SearchDocument.of(docid, docno, title, pbk, rank, score);
            searchResult.addDocument(document);
            rank++;
        }

        // remember to close the index and the directory
        index.close();
        dir.close();

        return searchResult;
    }

}
