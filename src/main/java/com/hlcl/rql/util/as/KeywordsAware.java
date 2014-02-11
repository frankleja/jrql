package com.hlcl.rql.util.as;


import com.hlcl.rql.as.Keyword;

import java.util.List;

public interface KeywordsAware {

    /**
     * Liefert alle keywords
     *
     * @return
     */
    public List<Keyword> getKeywords();

    /**
     * Liefert alle keywords mit einem bestimmten Wert. Wenn es mehrere keyworts mit dem gleichen Wert gibt, wird das erste Ergebnis zurückgegeben.
     *
     * @param value
     * @return
     */
    public Keyword getKeywordByValue(String value);

    /**
     * Liefert alle keywords für eine bestimmte category guid
     *
     * @param categoryGuid
     * @return
     */
    public List<Keyword> getKeywordsForCategoryGuid(String categoryGuid);

    /**
     * Liefert alle keywords für einen bestimmten category Namen
     *
     * @param categoryName
     * @return
     */
    public List<Keyword> getKeywordsForCategoryName(String categoryName);

}
