package gov.idaho.sboe.jpa.beans;


public class StatisticSessionLength {

    private String day;
    private Long sessionLength;
    
    public StatisticSessionLength() {
    }

    public void setSessionLength(long sessionLength)
    {
        this.sessionLength = sessionLength;
    }

    public Long getSessionLength()
    {
        return sessionLength;
    }

    public void setDay(String day)
    {
        this.day = day;
    }

    public String getDay()
    {
        return day;
    }
}
