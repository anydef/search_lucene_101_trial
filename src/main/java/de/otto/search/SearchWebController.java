package de.otto.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/search")
@Slf4j
public class SearchWebController {

    public SearchWebController(Searcher searcher) {
        this.searcher = searcher;
        log.debug("Im here!!!!!!!!!!!!!!!!");
    }

    private Searcher searcher;

    @GetMapping(path = "/retrieve")
    public String query() {
        return "NYI";
    }
}
