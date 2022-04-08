package de.otto.search;

import edu.wisc.ischool.wiscir.examples.BM25SimilarityOriginal;
import edu.wisc.ischool.wiscir.utils.LuceneUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class Searcher {

    public static void main( String[] args ) {
        new Searcher().retrieve();
    }

    public void retrieve() {
        try {

            String pathIndex = "example_otto_index";

            String field = "title"; // the field you hope to search for
            QueryParser parser = new QueryParser( field, new SimpleGermanAnalyzer() ); // a query parser that transforms a text string into Lucene's query object

            String qstr = "groß"; // this is the textual search query
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

            log.info(String.format( "%-10s%-20s%-10s%-40s%s\n", "Rank", "DocNo", "Score", "PBK", "Title" ));
            int rank = 1;
            for ( ScoreDoc scoreDoc : docs.scoreDocs ) {
                int docid = scoreDoc.doc;
                double score = scoreDoc.score;
                String docno = LuceneUtils.getDocno( index, "docno", docid );
                String pbk = LuceneUtils.getDocno( index, "pbk", docid );
                String title = LuceneUtils.getDocno( index, "title", docid );
                log.info(String.format( "%-10d%-20s%-10.4f%-40s%s\n", rank, docno, score, pbk, title ));
                rank++;
            }

            // remember to close the index and the directory
            index.close();
            dir.close();

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

}
