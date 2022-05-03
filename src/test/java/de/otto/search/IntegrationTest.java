package de.otto.search;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class IntegrationTest {

    @Autowired
    Indexer indexer;

    @Autowired
    Searcher searcher;

    @Autowired
    PropertyConfiguration configuration;

    @ParameterizedTest
    @MethodSource("getTestData")
    public void testQueryResults(String queryString, String[] expectedDocs) throws ParseException, IOException {

        final SearchResult result = searcher.retrieve(queryString);

        final List<String> foundDocs = result.getDocuments()
                .stream()
                .map(SearchDocument::getDocno)
                .collect(Collectors.toList());
        assertTrue(foundDocs.containsAll(Arrays.asList(expectedDocs)));
    }

    private static Stream<Arguments> getTestData() {
        return Stream.of(
                Arguments.arguments("gold", new String[]{
                        "S082Q0U1YMAA",
                        "S082Q0WE5G7U",
                        "S082Q0WYAILR",
                        "S082Q0RD3YQH",
                        "S082R0A0CVYU",
                        "S082R0A2512X"}),
                // S082R01PS6GB
                // S082Q0UGYN5G S082Q0W49D1Y S082Q0WE5G7U S082Q0WH9A7C S082Q0WYAILR S082Q0XVV38C S082Q0XWR49U
                // S082R016I1EA S082R01PS6GB S082R026PIM7 textfield basecolor
                // S082R032BQYZ gold as subword
                Arguments.arguments("pullover", new String[]{
                        "S082R02WBOJ8"
                }),
                Arguments.arguments("blaue strickjacke", new String[] {
                        "S082R048J552",
                        "S082R00Z5C80",
                        "S082R0482PJE"
                })
        );
    }
}