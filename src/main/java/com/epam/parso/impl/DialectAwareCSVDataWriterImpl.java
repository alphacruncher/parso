package com.epam.parso.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import com.epam.parso.Column;

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

    @Override
    public void writeRow(List<Column> columns, Object[] row) throws IOException {
        if (row == null) {
            return;
        }

        Writer writer = getWriter();
        for (int currentColumnIndex = 0; currentColumnIndex < columns.size(); currentColumnIndex++) {
            if (row[currentColumnIndex] != null) {
                if (row[currentColumnIndex].getClass().getName().compareTo(
                        (new byte[0]).getClass().getName()) == 0) {
                    writer.write("\"");
                    String trimmedText = new String((byte[]) row[currentColumnIndex], ENCODING);
                    String trimmedTextWithoutQuotesDuplicates = trimmedText.replace("\"", "\"\"");
                    writer.write(trimmedTextWithoutQuotesDuplicates);
                    writer.write("\"");
                } else {
                    processEntry(columns, row, currentColumnIndex);
                }
            } else {
                writer.write(dialect.getNullString());
            }
            if (currentColumnIndex != columns.size() - 1) {
                writer.write(getDelimiter());
            }
        }

        writer.write(getEndline());
        writer.flush();
    }

    @Override
    protected void processEntry(List<Column> columns, Object[] row,
            int currentColumnIndex) throws IOException {
        if (!String.valueOf(row[currentColumnIndex]).contains(DOUBLE_INFINITY_STRING)) {
            String valueToPrint;
            if (row[currentColumnIndex].getClass() == Date.class) {
                valueToPrint = convertDateElementToString((Date) row[currentColumnIndex],
                        columns.get(currentColumnIndex).getFormat());
            } else {
                if (TIME_FORMAT_STRINGS.contains(columns.get(currentColumnIndex).getFormat())) {
                    valueToPrint = convertTimeElementToString((Long) row[currentColumnIndex]);
                } else {
                    valueToPrint = String.valueOf(row[currentColumnIndex]);
                    if (row[currentColumnIndex].getClass() == Double.class) {
                        valueToPrint = convertDoubleElementToString((Double) row[currentColumnIndex]);
                    } else if (row[currentColumnIndex].getClass() == String.class) {
                        getWriter().write("\"");
                        String trimmedTextWithoutQuotesDuplicates = valueToPrint.replace("\"", "\"\"");
                        getWriter().write(trimmedTextWithoutQuotesDuplicates);
                        getWriter().write("\"");
                        return;
                    }
                }
            }
            checkSurroundByQuotesAndWrite(getWriter(), getDelimiter(), valueToPrint);
        }
    }



}
