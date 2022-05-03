package de.otto.search.xmlparsing;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.Reader;
import java.util.Iterator;

public class StreamBasedSimpleFeedIterator implements Iterator<XMLEventReader>, Iterable<XMLEventReader>, AutoCloseable {

    private final XMLEventReader reader;

    public StreamBasedSimpleFeedIterator(Reader productfeed) throws XMLStreamException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        reader = xmlInputFactory.createXMLEventReader(productfeed);
    }

    @Override
    public boolean hasNext() {
        while (reader.hasNext()) {
            try {
                if (reader.peek().isStartElement()
                        && reader.peek().asStartElement().getName().getLocalPart().equals("searchVariation")) {
                    return true;
                } else {
                    reader.nextEvent();
                }
            } catch (XMLStreamException e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }

    @Override
    public XMLEventReader next() {
        return reader;
    }

    @Override
    public Iterator<XMLEventReader> iterator() {
        return this;
    }

    @Override
    public void close() throws Exception {
        if (reader != null) {
            reader.close();
        }
    }
}
