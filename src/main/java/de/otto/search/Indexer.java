package de.otto.search;

import de.otto.search.xmlparsing.XmlParser;
import de.otto.srch.searchvariation.domain.SearchAttributes;
import de.otto.srch.searchvariation.domain.SearchBrand;
import de.otto.srch.searchvariation.domain.SearchProductTypes;
import de.otto.srch.searchvariation.domain.SearchVariation;
import edu.wisc.ischool.wiscir.examples.BM25SimilarityOriginal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class Indexer {

    private final PropertyConfiguration propertyConfiguration;

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        try {
            log.info("indexing ...");
            this.index(propertyConfiguration.getIndexPath(), propertyConfiguration.getCorpusPath());
            log.info("finished indexing");
        } catch (Exception e) {
            log.error("Something went wrong while indexing ...", e);
        }
    }

    public void index(final String indexPath, String corpusPath) throws IOException, JAXBException, XMLStreamException {
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
        final List<SearchVariation> variations = XmlParser.toSearchVariationsStreamBased(instream);

        log.info("unmarschall: {}", variations);

        instream.close();

        for (SearchVariation variation : variations) {

            final Optional<SearchBrand> brand = variation.getBrand();
            final SearchProductTypes productTypes = variation.getProductTypes();
            final SearchAttributes attributes = variation.getAttributes();

            // Create a Document object
            Document document = new Document();

            // Add each field to the document with the appropriate field type options
            document.add(new Field(FieldDefinition.DOCNO.getFieldName(), variation.getId(), fieldTypeMetadata));
            document.add(new Field(FieldDefinition.TITLE.getFieldName(), variation.getTitle(), fieldTypeText));
            document.add(new Field(FieldDefinition.PRODUKT_BASIS_KLASSE.getFieldName(), variation.getProduktBasisKlasse(), fieldTypeText));

            brand.ifPresent(searchBrand -> {
                document.add(new Field(FieldDefinition.BRAND.getFieldName(), searchBrand.getBrandName(), fieldTypeText));
            });

            Optional.ofNullable(productTypes)
                    .ifPresent(searchProductTypes -> {
                        final String types = String.join(" ", searchProductTypes.getProductTypes());
                        document.add(new Field(FieldDefinition.PRODUCT_TYPE.getFieldName(), types, fieldTypeText));
                    });

            attributes.getTextAttributes()
                    .stream()
                    .filter(searchAttributeText -> FieldDefinition.COLOR.getFieldName().equals(searchAttributeText.getName()))
                    .findAny()
                    .ifPresent(searchAttributeText -> {
                        final String colors = String.join(" ", searchAttributeText.getAttributeValues());
                        document.add(new Field(FieldDefinition.COLOR.getFieldName(), colors, fieldTypeText));
                    });


            log.info("indexing document: {} - {}", variation.getId(), variation.getTitle());
            ixwriter.addDocument(document);
        }

        log.info("finished adding docs ...");
        ixwriter.commit();

        // remember to close both the index writer and the directory
        ixwriter.close();
        dir.close();
    }
}
