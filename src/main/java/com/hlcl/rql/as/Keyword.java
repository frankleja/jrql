package com.hlcl.rql.as;

public class Keyword implements Comparable<Keyword>{

    private final String value;
    private final String guid;

    private KeywordCategory category;

    public Keyword(String guid, KeywordCategory category, String value) {
        this.guid = guid;
        this.category = category;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getGuid() {
        return guid;
    }

    public KeywordCategory getCategory() {
        return category;
    }

    public void setCategory(KeywordCategory category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Keyword{" +
                "value='" + value + '\'' +
                ", guid='" + guid + '\'' +
                ", category=" + category +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Keyword keyword = (Keyword) o;

        if (!category.equals(keyword.category)) return false;
        if (!guid.equals(keyword.guid)) return false;
        if (!value.equals(keyword.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + guid.hashCode();
        result = 31 * result + category.hashCode();
        return result;
    }

    @Override
    public int compareTo(Keyword o) {
        return this.value.compareTo(o.getValue());
    }
}
