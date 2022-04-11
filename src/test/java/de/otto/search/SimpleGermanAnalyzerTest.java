package de.otto.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

class SimpleGermanAnalyzerTest {

    SimpleGermanAnalyzer simpleGermanAnalyzer;

    @BeforeAll
    public void init() {
        simpleGermanAnalyzer = new SimpleGermanAnalyzer();
    }
    public void test() {
        final Analyzer.TokenStreamComponents pbkAnalyzer = simpleGermanAnalyzer.createComponents("pbk");
        final TokenStream tokenStream = pbkAnalyzer.getTokenStream();
    }
}