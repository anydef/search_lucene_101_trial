package de.otto.search;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Slf4j
class IntegrationTest {

    @Autowired
    Indexer indexer;

    @Autowired
    Searcher searcher;

    @Autowired
    PropertyConfiguration configuration;

    @ParameterizedTest
    @MethodSource("getTestData")
    public void testQueryGoldReturnsDocuments(String queryString, String[] expectedDocs) throws ParseException, IOException {

        final SearchResult result = searcher.retrieveByTitle(queryString);

        final List<String> foundDocs = result.getDocuments()
                .stream()
                .map(SearchDocument::getDocno)
                .collect(Collectors.toList());
        assertThat(foundDocs, contains(expectedDocs));
    }

    private static Stream<Arguments> getTestData() {
        return Stream.of(
                Arguments.arguments("gold",  new String[]{"S082Q0WDFWSC", "S082Q0P5S0BC", "S082R0A2MAH4", "S082R09UPWIF", "S082Q0WGFMHT", "S082Q0VPMEFK", "S082Q0XVV38C"})
        );
    }
}