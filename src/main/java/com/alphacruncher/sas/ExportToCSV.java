package com.alphacruncher.sas;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.epam.parso.CSVDataWriter;
import com.epam.parso.impl.DatabaseDialect;
import com.epam.parso.impl.DialectAwareCSVDataWriterImpl;
import com.epam.parso.impl.SasFileReaderImpl;

/**
 * @author daniel.sali@alphacruncher.com
 * sas7bdat to CSV converter class using
 * the CSVWriter functionality of Parso, using a MySQL dialect
 */
public final class ExportToCSV {
    /**
     * Private constructor.
     */
    private ExportToCSV() { }

    /**
     * The main function, the entry point of execution.
     *
     * @param args
     *            The paths to the input and output files.
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("The input SAS file name and the "
                    + "output CSV file name have to be passed as arguments.");
            return;
        }
        InputStream is;
        try {
            is = new FileInputStream(args[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        com.epam.parso.SasFileReader sasFileReader = new SasFileReaderImpl(is);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(args[1]), "utf-8"))) {
            CSVDataWriter csvDataWriter = new DialectAwareCSVDataWriterImpl(
                    writer, DatabaseDialect.MYSQL);
            csvDataWriter.writeColumnNames(sasFileReader.getColumns());
            Object[] data;
            while ((data = sasFileReader.readNext()) != null) {
                csvDataWriter.writeRow(sasFileReader.getColumns(), data);
            }
            writer.flush();
            writer.close();
            System.out.println("CSV successfully written to: " + args[1]);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

}
