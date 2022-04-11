package de.otto.search;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@JsonDeserialize
@Value
@RequiredArgsConstructor(staticName = "of")
public class SearchDocument {
    String title;
    String productBaseClass;
    Integer rank;
    Double BM25Score;
}
