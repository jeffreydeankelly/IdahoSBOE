package gov.idaho.sboe.utils;

import java.io.FileInputStream;

import java.sql.DriverManager;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;


public class DatabaseImport {
    private String userid = "CCAT_TEST";
    private String password = "CCAT_TEST";
    private String url = "jdbc:oracle:thin:@cde-dw-test.cde.state.co.us:1521:edwi";
    private String className = "oracle.jdbc.OracleDriver";
    public DatabaseImport() {
    }

    public DatabaseImport(String userid, String password, String url, String className) {
        this.userid = userid;
        this.password = password;
        this.url = url;
        this.className = className;
    }

    public void loadTheTables() throws Exception {
        // database connection
        Class.forName(className);
        IDatabaseConnection connection = new DatabaseConnection( DriverManager.getConnection(
                url, userid, password));
        // initialize your dataset here
        IDataSet dataSet = new FlatXmlDataSet(new FileInputStream("src/test/resources/mockdata/ccat_glossary_data.xml"));
        try
        {
               DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
               dataSet = new FlatXmlDataSet(new FileInputStream("src/test/resources/mockdata/ccat_de2de_data.xml"));
               DatabaseOperation.INSERT.execute(connection, dataSet);
        }
        finally
        {
               connection.close();
        }
    }
    
    public static void main(String args[]) {
        DatabaseImport di = new DatabaseImport();
        try {
            di.loadTheTables();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading: "+e.getMessage());
        }
    }
}
