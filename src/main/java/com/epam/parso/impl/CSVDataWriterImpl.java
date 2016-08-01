/**
 * *************************************************************************
 * Copyright (C) 2015 EPAM
 * <p>
 * This file is part of Parso.
 * <p>
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * <p>
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * *************************************************************************
 */

package com.epam.parso.impl;

import com.epam.parso.CSVDataWriter;
import com.epam.parso.Column;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

/**
 * This is a class to export the sas7bdat file data into the CSV format.
 */
public class CSVDataWriterImpl extends AbstractCSVWriter implements CSVDataWriter {
    /**
     * If the number of digits in a double value exceeds a given constant, it rounds off.
     */
    private static final int ROUNDING_LENGTH = 13;

    /**
     * The number of digits starting from the first non-zero value, used to round doubles.
     */
    private static final int ACCURACY = 15;

    /**
     * The constant to check whether or not a string containing double stores infinity.
     */
    protected static final String DOUBLE_INFINITY_STRING = "Infinity";

    /**
     * The format to output hours in the CSV format.
     */
    private static final String HOURS_OUTPUT_FORMAT = "%02d";

    /**
     * The format to output minutes in the the CSV format.
     */
    private static final String MINUTES_OUTPUT_FORMAT = "%02d";

    /**
     * The format to output seconds in the CSV format.
     */
    private static final String SECONDS_OUTPUT_FORMAT = "%02d";

    /**
     * The delimiter between hours and minutes, minutes and seconds in the CSV file.
     */
    private static final String TIME_DELIMETER = ":";

    /**
     * One of data formats used in sas7bdat files, corresponds to yyyy-MM-dd.
     */
    private static final String DATE_FORMAT_YYMMDD = "YYMMDD";

    /**
     * One of data formats used in sas7bdat files, corresponds to yyyyMMdd.
     */
    private static final String DATE_FORMAT_YYMMDDN = "YYMMDDN";

    /**
     * One of data formats used in sas7bdat files, corresponds to yyyy MM dd.
     */
    private static final String DATE_FORMAT_YYMMDDB = "YYMMDDB";

    /**
     * One of data formats used in sas7bdat files, corresponds to yyyy:MM:dd.
     */
    private static final String DATE_FORMAT_YYMMDDC = "YYMMDDC";

    /**
     * One of data formats used in sas7bdat files, corresponds to yyyy-MM-dd.
     */
    private static final String DATE_FORMAT_YYMMDDD = "YYMMDDD";

    /**
     * One of data formats used in sas7bdat files, corresponds to yyyy.MM.dd.
     */
    private static final String DATE_FORMAT_YYMMDDP = "YYMMDDP";

    /**
     * One of data formats used in sas7bdat files, corresponds to yyyy/MM/dd.
     */
    private static final String DATE_FORMAT_YYMMDDS = "YYMMDDS";

    /**
     * One of data formats used in sas7bdat files, corresponds to MM/dd/yyyy.
     */
    private static final String DATE_FORMAT_MMDDYY = "MMDDYY";

    /**
     * One of data formats used in sas7bdat files, corresponds to dd/MM/yyyy.
     */
    private static final String DATE_FORMAT_DDMMYY = "DDMMYY";

    /**
     * One of data formats used in sas7bdat files, corresponds to ddMMMyyyy.
     */
    private static final String DATE_FORMAT = "DATE";

    /**
     * One of data formats used in sas7bdat files, corresponds to yyyy-MM-dd HH:mm:ss.
     */
    private static final String DATE_TIME_FORMAT = "DATETIME";

    /**
     * The date formats to store the hour, minutes, seconds, and milliseconds. Appear in the data of
     * the {@link SasFileParser.FormatAndLabelSubheader} subheader and are stored in {@link Column#format}.
     */
    protected static final List<String> TIME_FORMAT_STRINGS = Arrays.asList("TIME", "HHMM");

    /**
     * The number of seconds in a minute.
     */
    private static final int SECONDS_IN_MINUTE = 60;

    /**
     * The number of minutes in an hour.
     */
    private static final int MINUTES_IN_HOUR = 60;

    /**
     * Encoding used to convert byte arrays to string.
     */
    protected static final String ENCODING = "CP1252";

    /**
     * The mapping between date formats in sas7bdat files and SimpleDateFormat.
     */
    private static final Map<String, String> DATE_OUTPUT_FORMAT_STRINGS;

    static {
        Map<String, String> tmpMap = new HashMap<String, String>();
        tmpMap.put(DATE_FORMAT_YYMMDD, "yyyy-MM-dd");
        tmpMap.put(DATE_FORMAT_YYMMDDN, "yyyyMMdd");
        tmpMap.put(DATE_FORMAT_YYMMDDB, "yyyy MM dd");
        tmpMap.put(DATE_FORMAT_YYMMDDC, "yyyy:MM:dd");
        tmpMap.put(DATE_FORMAT_YYMMDDD, "yyyy-MM-dd");
        tmpMap.put(DATE_FORMAT_YYMMDDP, "yyyy.MM.dd");
        tmpMap.put(DATE_FORMAT_YYMMDDS, "yyyy/MM/dd");
        tmpMap.put(DATE_FORMAT_MMDDYY, "MM/dd/yyyy");
        tmpMap.put(DATE_FORMAT_DDMMYY, "dd/MM/yyyy");
        tmpMap.put(DATE_FORMAT, "ddMMMyyyy");
        tmpMap.put(DATE_TIME_FORMAT, "yyyy-MM-dd HH:mm:ss");
        DATE_OUTPUT_FORMAT_STRINGS = Collections.synchronizedMap(tmpMap);
    }

    /**
     * The constructor that defines writer variable to output result csv file.
     *
     * @param writer the writer which is used to output csv file.
     */
    public CSVDataWriterImpl(Writer writer) {
        super(writer);
    }

    /**
     * The constructor that defines writer variable to output result csv file with selected delimiter.
     *
     * @param writer    the writer which is used to output csv file.
     * @param delimiter separator used in csv file.
     */
    public CSVDataWriterImpl(Writer writer, String delimiter) {
        super(writer, delimiter);
    }

    /**
     * The constructor that defines writer variable to output result csv file with selected delimiter and endline.
     *
     * @param writer    the writer which is used to output csv file.
     * @param delimiter separator used in csv file.
     * @param endline   symbols used in csv file as endline.
     */
    public CSVDataWriterImpl(Writer writer, String delimiter, String endline) {
        super(writer, delimiter, endline);
    }

    /**
     * The function to convert a date into a string according to the format used.
     *
     * @param currentDate the date to convert.
     * @param format      the string with the format that must belong to the set of
     *                    {@link CSVDataWriterImpl#DATE_OUTPUT_FORMAT_STRINGS} mapping keys.
     * @return the string that corresponds to the date in the format used.
     */
    protected static String convertDateElementToString(Date currentDate, String format) {
        SimpleDateFormat dateFormat;
        String valueToPrint = "";
        dateFormat = new SimpleDateFormat(DATE_OUTPUT_FORMAT_STRINGS.get(format));
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (currentDate.getTime() != 0) {
            valueToPrint = dateFormat.format(currentDate.getTime());
        }
        return valueToPrint;
    }

    /**
     * The function to convert time without a date (hour, minute, second) from the sas7bdat file format
     * (which is the number of seconds elapsed from the midnight) into a string of the format set by the constants:
     * {@link CSVDataWriterImpl#HOURS_OUTPUT_FORMAT}, {@link CSVDataWriterImpl#MINUTES_OUTPUT_FORMAT},
     * {@link CSVDataWriterImpl#SECONDS_OUTPUT_FORMAT}, and {@link CSVDataWriterImpl#TIME_DELIMETER}.
     *
     * @param secondsFromMidnight the number of seconds elapsed from the midnight.
     * @return the string of time in the format set by constants.
     */
    protected static String convertTimeElementToString(Long secondsFromMidnight) {
        return String.format(HOURS_OUTPUT_FORMAT, secondsFromMidnight / SECONDS_IN_MINUTE / MINUTES_IN_HOUR)
                + TIME_DELIMETER
                + String.format(MINUTES_OUTPUT_FORMAT, secondsFromMidnight / SECONDS_IN_MINUTE % MINUTES_IN_HOUR)
                + TIME_DELIMETER
                + String.format(SECONDS_OUTPUT_FORMAT, secondsFromMidnight % SECONDS_IN_MINUTE);
    }

    /**
     * The function to convert a double value into a string. If the text presentation of the double is longer
     * than {@link CSVDataWriterImpl#ROUNDING_LENGTH}, the rounded off value of the double includes
     * the {@link CSVDataWriterImpl#ACCURACY} number of digits from the first non-zero value.
     *
     * @param value the input numeric value to convert.
     * @return the string with the text presentation of the input numeric value.
     */
    protected static String convertDoubleElementToString(Double value) {
        String valueToPrint = String.valueOf(value);
        if (valueToPrint.length() > ROUNDING_LENGTH) {
            int lengthBeforeDot = (int) Math.ceil(Math.log10(Math.abs(value)));
            BigDecimal bigDecimal = new BigDecimal(value);
            bigDecimal = bigDecimal.setScale(ACCURACY - lengthBeforeDot, BigDecimal.ROUND_HALF_UP);
            valueToPrint = String.valueOf(bigDecimal.doubleValue());
        }
        valueToPrint = trimZerosFromEnd(valueToPrint);
        return valueToPrint;
    }

    /**
     * The function to remove trailing zeros from the decimal part of the numerals represented by a string.
     * If there are no digits after the point, the point is deleted as well.
     *
     * @param string the input string trailing zeros.
     * @return the string without trailing zeros.
     */
    private static String trimZerosFromEnd(String string) {
        return string.contains(".") ? string.replaceAll("0*$", "").replaceAll("\\.$", "") : string;
    }

    /**
     * The method to export a row from sas7bdat file (stored as an object of the {@link SasFileReaderImpl} class)
     * using {@link CSVDataWriterImpl#writer}.
     *
     * @param columns the {@link Column} class variables list that stores columns description from the sas7bdat file.
     * @param row     the Objects arrays that stores data from the sas7bdat file.
     * @throws java.io.IOException appears if the output into writer is impossible.
     */
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
                    checkSurroundByQuotesAndWrite(writer, getDelimiter(),
                            new String((byte[]) row[currentColumnIndex], ENCODING));
                } else {
                    processEntry(columns, row, currentColumnIndex);
                }
            } else {
                checkSurroundByQuotesAndWrite(writer, getDelimiter(), "");
            }
            if (currentColumnIndex != columns.size() - 1) {
                writer.write(getDelimiter());
            }
        }

        writer.write(getEndline());
        writer.flush();
    }

    /**
     * The method to export a parsed sas7bdat file (stored as an object of the {@link SasFileReaderImpl} class)
     * using {@link CSVDataWriterImpl#writer}.
     *
     * @param columns the {@link Column} class variables list that stores columns description from the sas7bdat file.
     * @param rows    the Objects arrays array that stores data from the sas7bdat file.
     * @throws java.io.IOException appears if the output into writer is impossible.
     */
    @Override
    public void writeRowsArray(List<Column> columns, Object[][] rows) throws IOException {
        for (Object[] currentRow : rows) {
            if (currentRow != null) {
                writeRow(columns, currentRow);
            } else {
                break;
            }
        }
    }

    /**
     * The method to output the column names using the {@link CSVDataWriterImpl#delimiter} delimiter
     * using {@link CSVDataWriterImpl#writer}.
     *
     * @param columns the list of column names.
     * @throws IOException appears if the output into writer is impossible.
     */
    @Override
    public void writeColumnNames(List<Column> columns) throws IOException {
        Writer writer = getWriter();
        for (int i = 0; i < columns.size(); i++) {
            checkSurroundByQuotesAndWrite(writer, getDelimiter(), columns.get(i).getName());
            if (i != columns.size() - 1) {
                writer.write(getDelimiter());
            }
        }
        writer.write(getEndline());
    }

    /**
     * Checks current entry type and write it into csv according to check result.
     * @param columns list of sas7bdat file columns.
     * @param row current processing row.
     * @param currentColumnIndex index of current entry in row;
     * @throws IOException appears if the output into writer is impossible.
     */
    protected void processEntry(List<Column> columns, Object[] row, int currentColumnIndex) throws IOException {
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
                    }
                }
            }

            checkSurroundByQuotesAndWrite(getWriter(), getDelimiter(), valueToPrint);
        }
    }
}
