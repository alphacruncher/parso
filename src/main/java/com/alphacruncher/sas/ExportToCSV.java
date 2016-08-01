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
import com.epam.parso.CSVMetadataWriter;
import com.epam.parso.impl.CSVDataWriterImpl;
import com.epam.parso.impl.CSVMetadataWriterImpl;
import com.epam.parso.impl.SasFileReaderImpl;

/**
 * @author daniel.sali@alphacruncher.com sas7bdat to CSV converter class using
 *         the CSVWriter functionality of Parso, using a MySQL dialect
 */
public final class ExportToCSV {
    /**
     * Private constructor.
     */
    private ExportToCSV() {
    }

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
        try (Writer stdOutWriter = new BufferedWriter(new OutputStreamWriter(
                System.out))) {
            stdOutWriter.write("Metadata for " + args[0] + ":\n");
            CSVMetadataWriter csvMetadataWriter = new CSVMetadataWriterImpl(stdOutWriter);
            csvMetadataWriter.writeMetadata(sasFileReader.getColumns());
            stdOutWriter.write("-----------------\n\n");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(args[1]), "utf-8"))) {
            CSVDataWriter csvDataWriter = new CSVDataWriterImpl(writer);
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
