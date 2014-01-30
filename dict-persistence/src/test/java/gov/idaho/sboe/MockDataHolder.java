package gov.idaho.sboe;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import gov.idaho.sboe.jpa.beans.Glossary;

public class MockDataHolder {
    /**
     * <p>Holds the data read from the classpath, "mockdata/glossary.csv" file.</p>
     */
    private List<String[]> glossaryData = null;

    /**
     * <p>Holds the data read from the classpath, "mockdata/glossary-type.csv" file.</p>
     */
    private List<String[]> glossaryTypeData = null;

    /**
     * <p>Holds the data read from the classpath, "mockdata/xref.csv" file.</p>
     */
    private List<String[]> glossaryXwalkData = null;

	public List<String[]> getGlossaryData() throws Exception {
		if (glossaryData == null) {
			loadGlossary();
		}
		return glossaryData;
	}

	public List<String[]> getGlossaryTypeData() throws Exception {
		if (glossaryTypeData == null) {
			loadGlossaryTypes();
		}
		return glossaryTypeData;
	}

	public List<String[]> getGlossaryXwalkData() throws Exception {
		if (glossaryXwalkData == null) {
			loadGlossaryXwalk();
		}
		return glossaryXwalkData;
	}

    /**
     * <p>Load mock data from "glossary.csv".</p>
     * @throws Exception error loading data
     */
    private void loadGlossary() throws Exception {
        InputStream in = null;
        BufferedReader reader = null;

        glossaryData = new ArrayList<String[]>();

        try {

            ClassLoader loader = 
                Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                loader = this.getClass().getClassLoader();
            }

            in = loader.getResourceAsStream("mockdata/glossary.csv");
            if (in == null) throw new FileNotFoundException("Unable to open mockdata/glossary.csv");
            
            reader = new BufferedReader(new InputStreamReader(in));


            String line = null;
            int lineNo = 0;
            do {
                line = reader.readLine();
                lineNo++;
                if (line == null || line.length() == 0) {
                    break;
                }
                line = line.trim();
                if (line.length() == 0) {
                    break;
                }

                String[] columns = line.split("\t");
                if (3 != columns.length) throw new Exception("Invalid number of columns.  Line: " + lineNo);
                glossaryData.add(columns);

            } while (true);

        } finally {
            if (reader != null) reader.close();
            if (in != null) in.close();
        }

    }

    
    /**
     * <p>Loads mock data from the "glossary-type.csv" file.</p>
     *
     * @throws Exception error loading file
     */
    private void loadGlossaryTypes() throws Exception {
        InputStream in = null;
        BufferedReader reader = null;

        glossaryTypeData = new ArrayList<String[]>();

        try {

            ClassLoader loader = 
                Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                loader = this.getClass().getClassLoader();
            }

            in = loader.getResourceAsStream("mockdata/glossary-type.csv");
            if (in == null) throw new FileNotFoundException("Unable to open mockdata/glossary-type.csv");

            reader = new BufferedReader(new InputStreamReader(in));


            String line = null;
            do {
                line = reader.readLine();
                if (line == null || line.length() == 0) {
                    break;
                }

                String[] columns = line.split("\t");
                if (2 != columns.length) throw new Exception("Invalid number of columns in glossary-type.csv");

                // fix some bad data
                StringBuffer buff = 
                    new StringBuffer(columns[0].toLowerCase().trim());
                for (int i = buff.length() - 1; i > 0; i--) {
                    if (buff.charAt(i) == ' ') {
                        buff.setCharAt(i + 1, 
                                       Character.toUpperCase(buff.charAt(i + 
                                                                         1)));
                    }
                }
                buff.setCharAt(0, Character.toUpperCase(buff.charAt(0)));

                columns[0] = buff.toString();
                glossaryTypeData.add(columns);

            } while (true);

        } finally {
            reader.close();
            in.close();
        }
    }

    /**
     * <p>Loads mock data from "xref.csv".</p>
     * @throws Exception error loading file
     */
    private void loadGlossaryXwalk() throws Exception {
        InputStream in = null;
        BufferedReader reader = null;

        glossaryXwalkData = new ArrayList<String[]>();

        try {
            ClassLoader loader = 
                Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                loader = this.getClass().getClassLoader();
            }

            in = loader.getResourceAsStream("mockdata/xref.csv");
            if (in == null) throw new FileNotFoundException("Unable to open mockdata/xref.csv");

            reader = new BufferedReader(new InputStreamReader(in));


            String line = null;
            int lineNo = 0;
            do {
                line = reader.readLine().trim();
                lineNo++;
                if (line == null || line.length() == 0) {
                    break;
                }

                String[] columns = line.split("\t");
                if (4 != columns.length) throw new Exception("Invalid number of columns. Line: " + lineNo);
                
                glossaryXwalkData.add(columns);

            } while (true);

        } finally {
            reader.close();
            in.close();
        }
    }

        /**
         * <p>Searches the relationships for parents to a glossary item.</p>
         * @param glossType glossary type
         * @param itemName glossary name
         * @return array of parent relationships
         * @throws Exception 
         */
        public Glossary.PK[] findParentsForChild(String glossType, String itemName) throws Exception {
            List<Glossary.PK> tmp = new ArrayList<Glossary.PK>();
            for (String[] columns: getGlossaryXwalkData()) {
               if (columns[2].equals(glossType) && columns[3].equals(itemName)) {
                   tmp.add(new Glossary.PK(columns[0], columns[1]));
               }
            }
            Glossary.PK[] parentKeys = new Glossary.PK[tmp.size()];
            tmp.toArray(parentKeys);
            return parentKeys;
        }

}
