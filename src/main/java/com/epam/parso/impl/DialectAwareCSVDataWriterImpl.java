package com.epam.parso.impl;

import java.io.IOException;
import java.io.Writer;

/**
 * @author salid
 * Writes the database dialect appropriate NULL strings to the CSV file when a field is null.
 */
public class DialectAwareCSVDataWriterImpl extends CSVDataWriterImpl {
    /**
     * The dialect used by the writer.
     */
    private DatabaseDialect dialect;

    /**
     * If the database dialect is not specified, defaults to the CSVDataWriterImpl.
     * @param writer    the writer which is used to output csv file.
     */
    public DialectAwareCSVDataWriterImpl(Writer writer) {
        super(writer);
    }

    /**
     * If the database dialect is not specified, defaults to the CSVDataWriterImpl.
     * @param writer    the writer which is used to output csv file.
     * @param dialect   the database dialect to use.
     */
    public DialectAwareCSVDataWriterImpl(Writer writer, DatabaseDialect dialect) {
        super(writer);
        this.dialect = dialect;
    }

    /**
     * The constructor that defines writer variable to output result csv file with selected delimiter.
     *
     * @param writer    the writer which is used to output csv file.
     * @param delimiter separator used in csv file.
     * @param dialect   the database dialect to use.
     */
    public DialectAwareCSVDataWriterImpl(Writer writer, String delimiter, DatabaseDialect dialect) {
        super(writer, delimiter);
        this.dialect = dialect;
    }

    /**
     * The constructor that defines writer variable to output result csv file with selected delimiter and endline.
     *
     * @param writer    the writer which is used to output csv file.
     * @param delimiter separator used in csv file.
     * @param endline   symbols used in csv file as endline.
     * @param dialect   the database dialect to use.
     */
    public DialectAwareCSVDataWriterImpl(Writer writer, String delimiter,
            String endline, DatabaseDialect dialect) {
        super(writer, delimiter, endline);
        this.dialect = dialect;
    }

    @Override
    protected void checkSurroundByQuotesAndWrite(Writer writer,
            String delimiter, String trimmedText) throws IOException {
        if (trimmedText != null && !trimmedText.isEmpty()) {
            boolean containsDelimiter = stringContainsItemFromList(trimmedText, delimiter, "\n", "\t", "\r", "\"");
            String trimmedTextWithoutQuotesDuplicates = trimmedText.replace("\"", "\"\"");
            if (containsDelimiter && trimmedTextWithoutQuotesDuplicates.length() != 0) {
                writer.write("\"");
            }
            writer.write(trimmedTextWithoutQuotesDuplicates);
            if (containsDelimiter && trimmedTextWithoutQuotesDuplicates.length() != 0) {
                writer.write("\"");
            }
        } else {
            writer.write(dialect.getNullString());
        }

    }
}
