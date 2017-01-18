package com.alphacruncher.sas;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.epam.parso.Column;
import com.epam.parso.impl.DatabaseDialect;

/**
 * @author salid
 *
 */
public class MySQLSchemaExporter extends SchemaExporter {

    /**
     * Creates a MySQLSchemaExporter instance.
     * @param schema The name of the database schema.
     * @param engine The storage engine to use.
     * @param charset The charset to use.
     * @param collation The collation to use.
     */
    protected MySQLSchemaExporter(String schema,
            String engine, String charset, String collation) {
        super(DatabaseDialect.MYSQL, schema, engine, charset,
                collation);
    }

    /**
     * @see com.alphacruncher.sas.SchemaExporter#addCreateTableStatement(java.lang.String,
     *         java.util.List, java.lang.StringBuffer)
     */
    @Override
    protected void addCreateTableStatement(String tableName, List<Column> columns,
            StringBuffer sb) {
        sb.append("CREATE TABLE IF NOT EXISTS ").append(getDialect().getEscapeString())
            .append(tableName).append(getDialect().getEscapeString()).append(" (")
            .append(System.getProperty("line.separator"));
        List<String> columnDefs = new ArrayList<String>(columns.size());
        for (Column c : columns) {
            StringBuffer csb = new StringBuffer(" ");
            csb.append(getDialect().getEscapeString()).append(c.getName())
                .append(getDialect().getEscapeString()).append(" ");
            csb.append(getDatabaseType(c)).append(" NULL DEFAULT NULL");
            if (StringUtils.isNotBlank(c.getLabel())) {
                csb.append(" COMMENT '").append(c.getLabel()).append("'");
            }
            columnDefs.add(csb.toString());
        }
        sb.append(StringUtils.join(columnDefs, "," + System.getProperty("line.separator")));
        sb.append(") ENGINE='").append(getEngine()).append("' CHARACTER SET ").append(getCharset())
            .append(" COLLATE ").append(getCollation())
            .append(";").append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"));
    }

    /**
     * @see com.alphacruncher.sas.SchemaExporter#addCreateSchemaStatement(java.lang.StringBuffer)
     */
    @Override
    protected void addCreateSchemaStatement(StringBuffer sb) {
        sb.append("CREATE DATABASE ").append(getDialect().getEscapeString())
            .append(getSchema()).append(getDialect().getEscapeString()).append(";")
            .append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"));
        sb.append("USE ").append(getDialect().getEscapeString())
            .append(getSchema()).append(getDialect().getEscapeString()).append(";")
            .append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"));
    }

}
