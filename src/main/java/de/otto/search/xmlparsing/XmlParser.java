package de.otto.search.xmlparsing;

import de.otto.srch.searchvariation.domain.SearchVariation;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class XmlParser {

    public static List<SearchVariation> toSearchVariationsStreamBased(InputStream stream) throws XMLStreamException, JAXBException {
        JAXBContext context = JAXBContext.newInstance(SearchVariation.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        StreamBasedSimpleFeedIterator feedInputStream = new StreamBasedSimpleFeedIterator(new InputStreamReader(stream));
        List<SearchVariation> result = new ArrayList<>();

        for (XMLEventReader xmlEventReader : feedInputStream) {
            Object searchVariation = unmarshaller.unmarshal(xmlEventReader);
            result.add((SearchVariation) searchVariation);
            log.info("Parsed variation - {}", ((SearchVariation) searchVariation).getId());
        }
        return result;
    }
}