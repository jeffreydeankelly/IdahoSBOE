package gov.idaho.sboe.utils;

import java.io.FileOutputStream;

import java.sql.Connection;
import java.sql.DriverManager;

import junit.framework.TestCase;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.database.search.TablesDependencyHelper;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

public class DatabaseExport extends TestCase {
    Class driverClass;
    Connection jdbcConnection;
    IDatabaseConnection connection;
    private final static String userid = "RESDAT\\jeffk";
    private final static String password = "j20552055K";

    public DatabaseExport() {
    }
    
    public void setUp() throws Exception
    {
        // database connection
        driverClass = Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        jdbcConnection = DriverManager.getConnection(
                "jdbc:sqlserver://localhost:1433;databaseName=SBOE_Data_Dictionary;integratedSecurity=true", userid, password);
        connection = new DatabaseConnection(jdbcConnection);
    }
    
    public void testDumpSomeTables() throws Exception {
        // partial database export
        QueryDataSet partialDataSet = new QueryDataSet(connection);
        partialDataSet.addTable("GlossaryType");
        partialDataSet.addTable("GlossaryWordAlias");
        partialDataSet.addTable("GlossaryNoiseWord");
        partialDataSet.addTable("DictUser");
        partialDataSet.addTable("Glossary");
        partialDataSet.addTable("GlossaryHistory");
        partialDataSet.addTable("GlossaryHistoryNarrIndx");
        partialDataSet.addTable("GlossaryNarrIndx");
        partialDataSet.addTable("GlossaryXwalk");
        partialDataSet.addTable("GlossaryXwalkHistory");
        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("partial.xml"));
    }
    public void testDumpFullDatabase() throws Exception {
        // full database export
        IDataSet fullDataSet = connection.createDataSet();
        FlatXmlDataSet.write(fullDataSet, new FileOutputStream("full.xml"));
    }
    
    public void testDump() throws Exception {
        // dependent tables database export: export table X and all tables that
        // have a PK which is a FK on X, in the right order for insertion
        String[] depTableNames = TablesDependencyHelper.getAllDependentTables( connection, "X" );
        IDataSet depDataSet = connection.createDataSet( depTableNames );
        FlatXmlDataSet.write(depDataSet, new FileOutputStream("dependents.xml"));
    }
}