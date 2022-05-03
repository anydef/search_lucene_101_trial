package de.otto.search.xmlparsing;

import de.otto.srch.searchvariation.domain.SearchVariation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Slf4j
class XmlParserTest {

    @Test
    public void unmarshalllTest() throws JAXBException, FileNotFoundException, XMLStreamException {
        String xmlPath = "example_search_variation_otto_partial.xml";
        InputStream instream = new FileInputStream(xmlPath);

        final List<SearchVariation> searchVariations = XmlParser.toSearchVariationsStreamBased(instream);
        log.info("result: {}", searchVariations);

        assertThat(searchVariations, is(not(empty())));

        searchVariations.forEach(searchVariation -> {
            assertThat(searchVariation.getId(), is(not(nullValue())));
            assertThat(searchVariation.getTitle(), is(not(nullValue())));
            assertThat(searchVariation.getAttributes(), is(not(nullValue())));
            assertThat(searchVariation.getProduktBasisKlasse(), is(not(nullValue())));
            assertThat(searchVariation.getArticleNumber(), is(not(nullValue())));
        });
    }
}