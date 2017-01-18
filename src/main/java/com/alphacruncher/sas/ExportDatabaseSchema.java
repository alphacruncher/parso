package com.alphacruncher.sas;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author salid Exports a MySQL/PostgreSQL database schema SQL script based on
 *         the table structure of .sas7bdat files in the given folder.
 */
public final class ExportDatabaseSchema {

    /**
     * The logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportDatabaseSchema.class);

    /**
     * Private constructor.
     */
    private ExportDatabaseSchema() {
    }

    /**
     * Entry point of the script. The following command line arguments have to
     * be specified:
     * --dialect 'MySQL' (default) or 'PostgreSQL'.
     * --schema The name of the database schema to use.
     * --engine The name of the storage engine used, 'InnoDB' is the default.
     * --charset The character set to use (default is 'latin1').
     * --collation The database collation to use (default is 'latin1_bin').
     * --folder The path of the folder where the .sas7bdat files are located.
     * --sql-file The name of the output SQL script file.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption(Option.builder("d").longOpt("dialect")
                .desc("'MySQL' (default) or 'PostgreSQL'.").hasArg().build());
        options.addOption(Option.builder("s").longOpt("schema").required()
                .desc("The name of the database schema to use.").hasArg().build());
        options.addOption(Option.builder("e").longOpt("engine")
                .desc("The name of the storage engine used, 'InnoDB' is the default.").hasArg().build());
        options.addOption(Option.builder("h").longOpt("charset")
                .desc("The character set to use (default is 'latin1').").hasArg().build());
        options.addOption(Option.builder("c").longOpt("collation")
                .desc("The database collation to use (default is 'latin1_bin').").hasArg().build());
        options.addOption(Option.builder("f").longOpt("folder").required()
                .desc("The path of the folder where the .sas7bdat files are located.").hasArg().build());
        options.addOption(Option.builder("q").longOpt("sql-file").required()
                .desc("The name of the output SQL script file.").hasArg().build());

        try {
            CommandLine line = parser.parse(options, args);
            String dialect = line.getOptionValue("d", SchemaExporter.DIALECT_MYSQL);
            String schema = line.getOptionValue("s");
            String engine = line.getOptionValue("e", SchemaExporter.ENGINE_INNODB);
            String charset = line.getOptionValue("h", SchemaExporter.CHARSET_LATIN1);
            String collation = line.getOptionValue("c", SchemaExporter.COLLATION_LATIN1_BIN);
            String folder = line.getOptionValue("f");
            String sqlFile = line.getOptionValue("q");

            StringBuffer sb = new StringBuffer();
            SchemaExporter schemaExporter = SchemaExporter.getExporterForDialect(dialect,
                    schema, engine, charset, collation);
            schemaExporter.exportSchema(folder, sb);
            String sqlScript = sb.toString();
            Charset utf8 = Charset.forName("UTF-8");
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(sqlFile), utf8)) {
                writer.write(sqlScript, 0, sqlScript.length());
            } catch (IOException x) {
                LOGGER.error("Failed to write SQL script file", x);
            }
        } catch (ParseException e) {
            LOGGER.error("Error while parsing command line arguments", e);
        }
    }

}
