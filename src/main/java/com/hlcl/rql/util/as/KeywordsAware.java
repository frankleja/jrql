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
     * Liefert alle keywords mit einem bestimmten Wert (das eigentliche Schlagwort). Wenn es mehrere keywords mit dem gleichen Wert gibt, wird das erste Ergebnis zurückgegeben.
     *
     * @param value Das eigentliche Schlagwort
     * @return
     */
    public Keyword getKeywordByValue(String value);

    /**
     * Liefert alle keywords für eine bestimmte category guid
     *
     * @param categoryGuid GUID der CMS Category
     * @return
     */
    public List<Keyword> getKeywordsForCategoryGuid(String categoryGuid);

    /**
     * Liefert alle keywords für einen bestimmten category Namen
     *
     * @param categoryName Bezeichner der Kategorie
     * @return
     */
    public List<Keyword> getKeywordsForCategoryName(String categoryName);

}
