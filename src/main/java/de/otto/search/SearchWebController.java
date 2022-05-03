package de.otto.search;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("/api/v1/search")
@Slf4j
public class SearchWebController {

    public SearchWebController(Searcher searcher) {
        this.searcher = searcher;
    }

    private final Searcher searcher;

    @GetMapping(path = "/query")
    @ResponseBody
    public SearchResult query(@RequestParam("query_term") @NonNull final String queryTerm) {
        try {
            return searcher.retrieve(queryTerm);
        } catch (ParseException | IOException e) {
            log.error("Something went wrong while searching ...");
            throw new IllegalStateException(e);
        }
    }
}
