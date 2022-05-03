package de.otto.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.de.GermanLightStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class SimpleGermanAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {

        // Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
        TokenStreamComponents ts = new TokenStreamComponents(new StandardTokenizer());
        // Step 2: transforming all tokens into lowercased ones (recommended for the majority of the problems)
        ts = new TokenStreamComponents(ts.getSource(), new LowerCaseFilter(ts.getTokenStream()));
        // Step 3: whether to remove stop words (unnecessary to remove stop words unless you can't afford the extra disk space)
        ts = new TokenStreamComponents(ts.getSource(), new StopFilter(ts.getTokenStream(), GermanAnalyzer.getDefaultStopSet()));
        // Step 4: whether to apply stemming
        ts = new TokenStreamComponents(ts.getSource(), new GermanLightStemFilter(ts.getTokenStream()));
        return ts;
    }
}
