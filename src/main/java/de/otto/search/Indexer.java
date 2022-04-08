package de.otto.search;

import edu.wisc.ischool.wiscir.examples.BM25SimilarityOriginal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class Indexer {

    private final PropertyConfiguration propertyConfiguration;

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        try {
            this.index(propertyConfiguration.getIndexPath(), propertyConfiguration.getCorpusPath());
        } catch (IOException e) {
            log.error("Something went wrong while indexing ...", e);
        }
    }

    public void index(final String indexPath, final String corpusPath) throws IOException {
        Directory dir = FSDirectory.open(new File(indexPath).toPath());

        final SimpleGermanAnalyzer simpleGermanAnalyzer = new SimpleGermanAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(simpleGermanAnalyzer);

        // Note that IndexWriterConfig.OpenMode.CREATE will override the original index in the folder
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        // Lucene's default BM25Similarity stores document field length using a "low-precision" method.
        // Use the BM25SimilarityOriginal to store the original document length values in index.
        final BM25SimilarityOriginal bm25 = new BM25SimilarityOriginal();
        config.setSimilarity(bm25);

        IndexWriter ixwriter = new IndexWriter(dir, config);

        // This is the field setting for metadata field (no tokenization, searchable, and stored).
        FieldType fieldTypeMetadata = new FieldType();
        fieldTypeMetadata.setOmitNorms(true);
        fieldTypeMetadata.setIndexOptions(IndexOptions.DOCS);
        fieldTypeMetadata.setStored(true);
        fieldTypeMetadata.setTokenized(false);
        fieldTypeMetadata.freeze();

        // This is the field setting for normal text field (tokenized, searchable, store document vectors)
        FieldType fieldTypeText = new FieldType();
        fieldTypeText.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        fieldTypeText.setStoreTermVectors(true);
        fieldTypeText.setStoreTermVectorPositions(true);
        fieldTypeText.setTokenized(true);
        fieldTypeText.setStored(true);
        fieldTypeText.freeze();

        // You need to iteratively read each document from the example corpus file,
        // create a Document object for the parsed document, and add that
        // Document object by calling addDocument().

        // Well, the following only works for small text files. DO NOT follow this part for large dataset files.
        InputStream instream = new GZIPInputStream(new FileInputStream(corpusPath));
        String corpusText = new String(IOUtils.toByteArray(instream), "UTF-8");
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

        Matcher matcher = pattern.matcher(corpusText);

        while (matcher.find()) {

            String docno = matcher.group(1).trim();
            String title = matcher.group(2).trim();
            String brand = matcher.group(3).trim();
            String pbk = matcher.group(4).trim();
            String ptype = matcher.group(5).trim();

            // Create a Document object
            Document d = new Document();
            // Add each field to the document with the appropriate field type options
            d.add(new Field("docno", docno, fieldTypeMetadata));
            d.add(new Field("title", title, fieldTypeText));
            d.add(new Field("brand", brand, fieldTypeText));
            d.add(new Field("pbk", pbk, fieldTypeText));
            d.add(new Field("ptype", ptype, fieldTypeText));
            // Add the document to the index
            log.info("indexing document {} - {}", docno, pbk);
            ixwriter.addDocument(d);
        }

        log.info("finished adding docs ...");
        ixwriter.commit();

        // remember to close both the index writer and the directory
        ixwriter.close();
        dir.close();
    }
}
