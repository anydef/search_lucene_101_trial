package de.otto.search;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertyConfiguration {

    @Value("${de.otto.search.index-path}")
    @Getter
    private String indexPath;

    @Value("${de.otto.search.corpus-path}")
    @Getter
    private String corpusPath;
}
