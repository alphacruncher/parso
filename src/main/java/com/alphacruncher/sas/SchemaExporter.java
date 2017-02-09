package com.alphacruncher.sas;

import static com.epam.parso.impl.SasFileConstants.DATETIME_FORMATS;
import static com.epam.parso.impl.SasFileConstants.DATE_FORMATS;
import static com.epam.parso.impl.SasFileConstants.TIME_FORMATS;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.parso.Column;
import com.epam.parso.SasFileReader;
import com.epam.parso.impl.DatabaseDialect;
import com.epam.parso.impl.SasFileReaderImpl;

/**
 * @author Daniel Sali [daniel.sali@alphacruncher.com]
 * Exports a database schema for .sas7bdat files in a folder using
 * a database dialect.
 */
public abstract class SchemaExporter {

    /**
     * The logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaExporter.class);

    /**
     * MySQL dialect constant.
     */
    static final String DIALECT_MYSQL = "MySQL";

    /**
     * PostgreSQL dialect constant.
     */
    static final String DIALECT_POSTGRES = "PostgreSQL";

    /**
     * InnoDB engine constant.
     */
    static final String ENGINE_INNODB = "InnoDB";

    /**
     * Latin1 chracter set constant.
     */
    static final String CHARSET_LATIN1 = "latin1";

    /**
     * Latin1_bin collation constant.
     */
    static final String COLLATION_LATIN1_BIN = "latin1_bin";

    /**
     * The database dialect to use to export the schema of the .sas7bdat files.
     */
    private DatabaseDialect dialect;

    /**
     * The name of the exported schema.
     */
    private String schema;

    /**
     * The storage engine used by the tables in the schema.
     */
    private String engine;

    /**
     * The character set used by character columns in the schema.
     */
    private String charset;

    /**
     * The collation used by character columns in the schema.
     */
    private String collation;

    /**
     * Creates a SchemaExporter using the given dialect.
     * @param dialect The database dialect to use.
     * @param schema The name of the database schema.
     * @param engine The storage engine to use.
     * @param charset The character set to use.
     * @param collation The collation to use.
     */
    protected SchemaExporter(DatabaseDialect dialect, String schema,
            String engine, String charset, String collation) {
        this.dialect = dialect;
        this.schema = schema;
        this.engine = engine;
        this.charset = charset;
        this.collation = collation;
    }

    /**
     * @return the dialect
     */
    protected DatabaseDialect getDialect() {
        return dialect;
    }

    /**
     * @param dialect the dialect to set
     */
    protected void setDialect(DatabaseDialect dialect) {
        this.dialect = dialect;
    }

    /**
     * @return the schema
     */
    protected String getSchema() {
        return schema;
    }

    /**
     * @param schema the schema to set
     */
    protected void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * @return the engine
     */
    protected String getEngine() {
        return engine;
    }

    /**
     * @param engine the engine to set
     */
    protected void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * @return the charset
     */
    protected String getCharset() {
        return charset;
    }

    /**
     * @param charset the charset to set
     */
    protected void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * @return the collation
     */
    protected String getCollation() {
        return collation;
    }

    /**
     * @param collation the collation to set
     */
    protected void setCollation(String collation) {
        this.collation = collation;
    }

    /**
     * Adds a CREATE TABLE statement to the StringBuffer to create a table where the data of
     * a .sas7bdat file can be loaded, based on the columns of the file.
     * @param tableName The name of the table to be created.
     * @param columns The columns in the .sas7bdat file.
     * @param sb The StringBuffer containing the SQL script.
     */
    protected abstract void addCreateTableStatement(final String tableName,
            final List<Column> columns, final StringBuffer sb);

    /**
     * Adds the CREATE DATABASE statement to the StringBuffer to create the database
     * for the tables.
     * @param sb The StringBuffer containing the SQL script.
     */
    protected abstract void addCreateSchemaStatement(final StringBuffer sb);

    /**
     * Adds the CREATE DATABASE statement to create the schema and
     * the CREATE TABLE statement for each .sas7bdat file in the given folder
     * to the SQL script.
     * @param folder The folder containing the .sas7bdat files.
     * @param sb The StringBuffer containing the SQL script.
     */
    public void exportSchema(final String folder, final StringBuffer sb) {
        Path dir = Paths.get(folder);
        addCreateSchemaStatement(sb);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.sas7bdat")) {
            for (Path file: stream) {
                LOGGER.info("Processing :" + file.getFileName());
                SasFileReader sasFileReader =
                        new SasFileReaderImpl(new FileInputStream(file.toFile()));
                addCreateTableStatement(file.getFileName().toString()
                        .replaceAll(".sas7bdat", ""),
                        sasFileReader.getColumns(), sb);
            }
        } catch (IOException | DirectoryIteratorException x) {
            LOGGER.error("Error while listing .sas7bdat files in folder", x);
        }
    }

    /**
     * Returns the corresponding database type for the given .sas7bdat file column.
     * @param c The .sas7bdat file column.
     * @return The database column type corresponding to the .sas7bdat file column.
     */
    protected String getDatabaseType(final Column c) {
        if (Number.class.equals(c.getType())) {
            if (c.getLength() <= 2) {
                return dialect.getIntType();
            }
            if (StringUtils.isBlank(c.getFormat())) {
                return dialect.getNumericType()
                        + dialect.getDefaultPrecision();
            }
            for (Pattern p : DATETIME_FORMATS) {
                Matcher m = p.matcher(c.getFormat());
                if (m.matches()) {
                    return dialect.getDateTimeType() + "(3)";
                }
            }
            for (Pattern p : TIME_FORMATS) {
                Matcher m = p.matcher(c.getFormat());
                if (m.matches()) {
                    return dialect.getTimeType() + "(3)";
                }
            }
            for (Pattern p : DATE_FORMATS) {
                Matcher m = p.matcher(c.getFormat());
                if (m.matches()) {
                    return dialect.getDateType();
                }
            }
            LOGGER.warn("Couldn't determine column format, defaulting to numeric: "
                    + c.getName() + "\t" + c.getFormat());
            return dialect.getNumericType() + dialect.getDefaultPrecision();
        } else {
            return dialect.getVarcharType() + "(" + c.getLength() + ")";
        }
    }

    /**
     * Returns a SchemaExporter instance for a supported database dialect.
     * @param dialect The database dialect to use.
     * Currently 'MySQL' and 'PostgreSQL' are supported.
     * @param schema The name of the database schema.
     * @param engine The storage engine to use.
     * @param charset The character set to use.
     * @param collation The collation to use.
     * @return The SchemaExporter instance.
     */
    public static SchemaExporter getExporterForDialect(String dialect,
            String schema, String engine, String charset, String collation) {
        if (StringUtils.isBlank(dialect)) {
            throw new RuntimeException("No database dialect given.");
        }
        if (DIALECT_MYSQL.equals(dialect)) {
            return new MySQLSchemaExporter(schema, engine, charset, collation);
        } else if (DIALECT_POSTGRES.equals(dialect)) {
            return new PostgreSQLSchemaExporter(schema, engine, charset, collation);
        } else {
            throw new RuntimeException("Unsupported dialect: " + dialect);
        }
    }

}
