package gov.idaho.sboe.view.utils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import java.util.Date;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import oracle.adf.view.faces.component.core.data.CoreTree;
import oracle.adf.view.faces.model.TreeModel;

import gov.idaho.sboe.services.FacadeException;
import gov.idaho.sboe.view.backing.CatalogDataAccessHelper;
import gov.idaho.sboe.view.backing.CatalogElementBean;
import gov.idaho.sboe.view.backing.Globals;

public class PrintHelper {
    private static final Logger log = Logger.getLogger(PrintHelper.class.getName());
    private static final String NOOPENNODES_MESSAGE = "Please open one or more Collections to print";

    public PrintHelper() {
    }
    
    public void doPrint(TreeModel treeModel, CoreTree tree, CatalogDataAccessHelper dataHelper) {
        Set<ArrayList> s = tree.getTreeState().getKeySet();
        List<CatalogElementBean> instance = (List<CatalogElementBean>)treeModel.getWrappedData();
        List<CatalogElementBean> openNodes = new ArrayList<CatalogElementBean>();
        for (ArrayList<String> o:s) {
            showOpenNode(o, instance, openNodes, dataHelper);
        }
        if (openNodes.size()== 0) {
            dataHelper.registerErrorMessage(NOOPENNODES_MESSAGE);
        } else {
            runReportIText(openNodes);
        }
    }
    
    void showOpenNode(ArrayList<String> path, List<CatalogElementBean> tree, List<CatalogElementBean> openNodes,
        CatalogDataAccessHelper dataHelper) {
        // each element of the path is an index into the children array
        CatalogElementBean currentElement = null;
        List<CatalogElementBean> currentTree = tree;
        for (String s: path) {
            int index = Integer.parseInt(s);
            currentElement = currentTree.get(index);
            currentTree = currentElement.getChildren();
        }
        log.finest("This node is expanded: "+currentElement.getDisplayItemName());
        if (!currentElement.getIsGlossaryType()) {
            openNodes.add(currentElement);
            try {
                currentElement.loadNarrative(dataHelper);
            } catch (FacadeException e) {
                // ignore so we get the line in the report anyway
                log.log(Level.INFO, "Facade exception loading narratice", e);
            }
            for (CatalogElementBean ceb: currentElement.getChildren()) {
                try {
                    ceb.loadNarrative(dataHelper);
                } catch (FacadeException e) {
                    // ignore so we get the line in the report anyway
                     log.log(Level.INFO, "Facade exception loading narratice", e);
                }
                openNodes.add(ceb);
            }
        }
    }

    protected HashMap createMapRow(CatalogElementBean ceb) {
        HashMap rowMap = new HashMap();
        rowMap.put("GlossaryType", ceb.getItemType());
        rowMap.put("ItemName", ceb.getItemName());
        rowMap.put("Narrative", ceb.getItemNarrative(false));
        return rowMap;
    }
    
    /**
     * iText-only version
     */
    void runReportIText(List<CatalogElementBean> openNodes) {
        try {
            // step 1: creation of a document-object
            Document document = new Document(PageSize.LETTER);
            document.setMarginMirroring(false);
            // step 2:
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new EndPage());
            boolean atStartOfPage = true;
            // step 3: we open the document
            document.open();
            // step 4: we add a paragraph to the document
            for (CatalogElementBean ceb: openNodes) {
                Paragraph p = null;
                if (ceb.getItemType().equals(Globals.COLLECTIONS_TYPE)) {
                    if (!atStartOfPage) {
                        document.newPage();
                        atStartOfPage = true;
                    }
                    p = new Paragraph(Globals.COLLECTIONS_TYPE+" "+ceb.getDisplayItemName(),
                        FontFactory.getFont(FontFactory.HELVETICA, 14));
                    p.setAlignment(Element.ALIGN_CENTER);
                    p.setSpacingBefore(18f);
                } else
                if (ceb.getItemType().equals(Globals.DATA_ELEMENT_TYPE)) {
                    p = new Paragraph(Globals.DATA_ELEMENT_TYPE+": "+ceb.getDisplayItemName(),
                        FontFactory.getFont(FontFactory.HELVETICA, 12));
                    p.setAlignment(Element.ALIGN_LEFT);
                    p.setSpacingBefore(18f);
                 } else {
                     p = new Paragraph(ceb.getItemType()+": "+ceb.getDisplayItemName(),
                         FontFactory.getFont(FontFactory.HELVETICA, 12));
                     p.setAlignment(Element.ALIGN_LEFT);
                       p.setSpacingBefore(18f);
                }                
                if (p != null) {
                    document.add(p);
                    atStartOfPage = false;
                }
                try {
                    StyleSheet style = new StyleSheet();
                    style.loadTagStyle("p", "font", "helvetica,10");
//                    parser.parse(new StringReader(/*applyHtmlToXmlFixups(*/ceb.getItemNarrative()));
                    // use the parseToList version so we can
                    // set the indent on the paragraph.
                    ArrayList list = HTMLWorker.parseToList(
                            new StringReader(ceb.getItemNarrative(false)),
                            style,
                            new HashMap());
                    Iterator iter = list.iterator();
                    p = new Paragraph();
                    p.setFont(FontFactory.getFont(FontFactory.HELVETICA, 10));
                    p.setIndentationLeft(30f);
                    while (iter.hasNext()) {
                        Element e = (Element)iter.next();
                        p.add(e);
                    }
                    document.add(p);
                } catch (Exception spe) {
                    System.out.println(spe.getMessage()+" - "+ceb.getItemType()+"|"+ceb.getItemName());
                    p = new Paragraph(ceb.getItemNarrative(false),
                        FontFactory.getFont(FontFactory.HELVETICA, 10));
                    p.setAlignment(Element.ALIGN_LEFT);
                    p.setIndentationLeft(30f);
                    document.add(p);
                }
            }
            // step 5: we close the document
            document.close();
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) context
                .getExternalContext().getResponse();
            response.setContentType("application/pdf");
            response.addHeader("Content-Disposition", "inline");
            response.setContentLength(baos.size());
            ServletOutputStream out = response.getOutputStream();
            baos.writeTo(out);
            out.flush();
            out.close();
        } catch (DocumentException de) {
                System.err.println(de.getMessage());
        } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
        }

    }
    
    String applyHtmlToXmlFixups(String input) {
//        Pattern p = Pattern.compile(".*\\<br\\>");
        return input.replace("<br>", "<br/>").replace("<BR>", "<BR/>");
    }
    
    /**
    * Demonstrates the use of PageEvents.
    */
    public class EndPage extends PdfPageEventHelper {
        private String imagePath;

        public EndPage() {
            ServletContext c = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
            imagePath = c.getRealPath("/") + "/images/banner.gif";
        }
        /**
        * @see com.lowagie.text.pdf.PdfPageEventHelper#onEndPage(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document)
        */
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                Rectangle page = document.getPageSize();
                PdfPTable head = new PdfPTable(2);
                Image jpg = Image.getInstance(imagePath);
                jpg.setAlignment(Image.LEFT);
                jpg.setBorder(0);
                head.addCell(jpg);

                PdfPCell cell = new PdfPCell(new Phrase("Data Dictionary",
                    FontFactory.getFont(FontFactory.HELVETICA, 17f)));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorder(0);
                head.addCell(cell);
                head.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                head.writeSelectedRows(0, -1, 
                    document.leftMargin(), page.getHeight() - document.topMargin() + head.getTotalHeight(),
                    writer.getDirectContent());
        
                PdfPTable foot = new PdfPTable(1);
                cell = new PdfPCell(new Phrase("Printed on "+(SimpleDateFormat.getDateTimeInstance().format(new Date()))));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(0);
                foot.addCell(cell);
                foot.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                foot.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin(),
                        writer.getDirectContent());
            }
            catch (Exception e) {
               throw new ExceptionConverter(e);
            }
        }
    }
}
