package de.otto.search;


import lombok.Getter;

@Getter
public enum FieldDefinition {
    DOCNO("docno"),
    PRODUCT_TYPE("ptype"),
    TITLE("title"),
    PRODUKT_BASIS_KLASSE("pbk"),
    BRAND("brand");
    final private String fieldName;

    FieldDefinition(String name) {
        this.fieldName = name;
    }
}
