package com.epam.parso.impl;

/**
 * @author daniel.sali@alphacruncher.com
 * An enumeration for different database dialects.
 * The database dialect determines how a NULL field is written to
 * or read from a CSV file.
 */
public enum DatabaseDialect {
    /**
     * Default dialect: the MYSQL dialect.
     */
    DEFAULT("\\N", "`", "int", "decimal", "(18,6)",  "varchar", "date", "time", "datetime"),
    /**
     * MySQL dialect: \N as NULL string.
     */
    MYSQL("\\N", "`", "int", "decimal", "(18,6)", "varchar", "date", "time", "datetime"),
    /**
     * PostgreSQL dialect: \N as NULL string.
     */
    POSTGRESQL("\\N", "\"", "integer", "numeric", "(18,6)", "varchar", "date", "time", "timestamp");

    /**
     * The string representing the NULL value in the dialect.
     */
    private String nullString;

    /**
     * The escape string used to quote column names which would be reserved keywords.
     */
    private String escapeString;

    /**
     * The int type used by the database.
     */
    private String intType;

    /**
     * The numeric type used by the database.
     */
    private String numericType;

    /**
     * The default numeric precision.
     */
    private String defaultPrecision;

    /**
     * The varchar type used by the database.
     */
    private String varcharType;

    /**
     * The date type used by the database.
     */
    private String dateType;

    /**
     * The time type used by the database.
     */
    private String timeType;

    /**
     * The datetime type used by the database.
     */
    private String dateTimeType;

    /**
     * Creates a new DatabaseDialect using the given NULL string.
     * @param nullString The NULL value in the dialect.
     * @param escapeString The escape string used by the dialect.
     * @param intType The integer type used by the dialect.
     * @param numericType The numeric type used by the dialect.
     * @param defaultPrecision The default numeric precision.
     * @param varcharType The varchar type used by the dialect.
     * @param dateType The date type used by the dialect.
     * @param timeType The time type used by the dialect.
     * @param dateTimeType The datetime type used by the dialect.
     */
    private DatabaseDialect(String nullString, String escapeString,
            String intType, String numericType, String defaultPrecision,
            String varcharType, String dateType, String timeType,
            String dateTimeType) {
        this.nullString = nullString;
        this.escapeString = escapeString;
        this.intType = intType;
        this.numericType = numericType;
        this.defaultPrecision = defaultPrecision;
        this.varcharType = varcharType;
        this.dateType = dateType;
        this.timeType = timeType;
        this.dateTimeType = dateTimeType;
    }

    /**
     * Gets the NULL value string used by the dialect.
     * @return The NULL value in the dialect.
     */
    public String getNullString() {
        return nullString;
    }

    /**
     * Gets the escape string used by the dialect.
     * @return The escape string of the dialect.
     */
    public String getEscapeString() {
        return escapeString;
    }

    /**
     * Gets the int type used by the dialect.
     * @return The int type used by the database.
     */
    public String getIntType() {
        return intType;
    }

    /**
     * Gets the numeric type used by the dialect.
     * @return The numeric type used by the database.
     */
    public String getNumericType() {
        return numericType;
    }

    /**
     * Gets the default numeric precision.
     * @return The default numeric precision.
     */
    public String getDefaultPrecision() {
        return defaultPrecision;
    }

    /**
     * Gets the varchar type used by the dialect.
     * @return The varchar type used by the database.
     */
    public String getVarcharType() {
        return varcharType;
    }

    /**
     * Gets the date type used by the dialect.
     * @return The date type used by the database.
     */
    public String getDateType() {
        return dateType;
    }

    /**
     * Gets the time type used by the dialect.
     * @return The time type used by the database.
     */
    public String getTimeType() {
        return timeType;
    }

    /**
     * Gets the datetime type used by the dialect.
     * @return The datetime type used by the database.
     */
    public String getDateTimeType() {
        return dateTimeType;
    }

}
