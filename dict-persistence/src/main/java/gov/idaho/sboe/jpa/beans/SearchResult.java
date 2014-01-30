package gov.idaho.sboe.jpa.beans;

import java.math.BigDecimal;

public class SearchResult 
    implements Comparable<SearchResult> {
    private BigDecimal score;
    private Glossary.PK item;
    private String narrative;

    public SearchResult(BigDecimal score, String gloss_type, String item_name, String narr) {
        this.score = score;
        this.item = new Glossary.PK(gloss_type, item_name);
        this.narrative = narr;
    }
    
    public int compareTo(SearchResult other) {
        int res = score.compareTo(other.score);
        if (res != 0) return res;
        return item.compareTo(other.item);
    }

    public BigDecimal getScore() {
        return score;
    }

    public Glossary.PK getItem() {
        return item;
    }

    public String getNarrative() {
        return narrative;
    }
    public boolean getIsLinkable() {
        return item.getGlossType().equals("Collection") || item.getGlossType().equals("Data Element");
    }
}
