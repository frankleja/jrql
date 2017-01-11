package com.hlcl.rql.as;

public class KeywordCategory {

    private final String name;
    private final String guid;

    public KeywordCategory(String guid, String name) {
        this.name = name;
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public String getGuid() {
        return guid;
    }

    @Override
    public String toString() {
        return "KeywordCategory{" +
                "name='" + name + '\'' +
                ", guid='" + guid + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeywordCategory category = (KeywordCategory) o;

        if (!guid.equals(category.guid)) return false;
        if (!name.equals(category.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + guid.hashCode();
        return result;
    }
}
