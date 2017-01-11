package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Keyword;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class RqlKeywordObject implements KeywordsAware{

    protected List<Keyword> keywords;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Keyword> getKeywords() {

        if(keywords == null) {
            loadKeywords();
        }

        return Collections.<Keyword>unmodifiableList(keywords);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Keyword> getKeywordsForCategoryGuid(String categoryGuid) {

        List<Keyword> filteredKeyords = new ArrayList<Keyword>();

        if(keywords == null) {
            loadKeywords();
        }

        for (Keyword keyword : keywords) {
            if(keyword.getCategory().getGuid().equals(categoryGuid)){
                filteredKeyords.add(keyword);
            }
        }

        return Collections.<Keyword>unmodifiableList(filteredKeyords);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Keyword> getKeywordsForCategoryName(String categoryName) {
        List<Keyword> filteredKeyords = new ArrayList<Keyword>();

        if(keywords == null) {
            loadKeywords();
        }

        for (Keyword keyword : keywords) {
            if(keyword.getCategory().getName().equals(categoryName)){
                filteredKeyords.add(keyword);
            }
        }

        return Collections.<Keyword>unmodifiableList(filteredKeyords);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Keyword getKeywordByValue(String value) {

        Keyword keywordFound = null;

        if(keywords == null) {
            loadKeywords();
        }

        for (Keyword keyword : keywords) {
            if(keyword.getValue().equals(value)){
                keywordFound = keyword;
                break;
            }
        }

        return keywordFound;
    }

    /**
     * Diese Methode implementiert, wie die Keywords aus dem CMS geladen werden.
     */
    protected abstract void loadKeywords();
}
