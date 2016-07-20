package com.epam.parso.impl;

/**
 * @author daniel.sali@alphacruncher.com
 * An enumeration for different database dialects.
 * The database dialect determines how a NULL field is written to
 * or read from a CSV file.
 */
public enum DatabaseDialect {
    /**
     * Default dialect: empty NULL string.
     */
    DEFAULT(""),
    /**
     * MySQL dialect: \N as NULL string.
     */
    MYSQL("\\N"),
    /**
     * PostgreSQL dialect: \N as NULL string.
     */
    POSTGRESQL("\\N");

    /**
     * The string representing the NULL value in the dialect.
     */
    private String nullString;

    /**
     * Creates a new DatabaseDialect using the given NULL string.
     * @param nullString The NULL value in the dialect.
     */
    private DatabaseDialect(String nullString) {
        this.nullString = nullString;
    }

    /**
     * Gets the NULL value string used by the dialect.
     * @return The NULL value in the dialect.
     */
    public String getNullString() {
        return nullString;
    }
}
