package de.otto.search;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@JsonDeserialize
@Value
@RequiredArgsConstructor(staticName = "of")
public class SearchDocument {
    Integer documentId;
    String docno;
    String title;
    String productBaseClass;
    Integer rank;
    Double BM25Score;
    String color;
}
