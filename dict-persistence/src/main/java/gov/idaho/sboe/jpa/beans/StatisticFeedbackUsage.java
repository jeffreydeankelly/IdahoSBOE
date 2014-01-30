package gov.idaho.sboe.jpa.beans;

import java.sql.Timestamp;


public class StatisticFeedbackUsage  
{
    private String userRate;
    private Timestamp usageDate;
    private String usagePage;
    private String usageExtra;
    private String userComment;
    

    public StatisticFeedbackUsage() {
    }


    public void setUserRate(String userRate)
    {
        this.userRate = userRate;
    }

    public String getUserRate()
    {
        return userRate;
    }

    public void setUsageDate(Timestamp usageDate)
    {
        this.usageDate = usageDate;
    }

    public Timestamp getUsageDate()
    {
        return usageDate;
    }

    public void setUsagePage(String usagePage)
    {
        this.usagePage = usagePage;
    }

    public String getUsagePage()
    {
        return usagePage;
    }

    public void setUsageExtra(String usageExtra)
    {
        this.usageExtra = usageExtra;
    }

    public String getUsageExtra()
    {
        return usageExtra;
    }


    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    public String getUserComment() {
        return userComment;
    }
}


