package gov.idaho.sboe.jpa.beans;


public class StatisticSessionPerDay {

	private String day;
    private String hour;
    private Long count;
    
    public StatisticSessionPerDay() {
    }

    public void setDay(String day)
    {
        this.day = day;
    }

    public String getDay()
    {
        return day;
    }

    public void setCount(Long count)
    {
        this.count = count;
    }

    public Long getCount()
    {
        return count;
    }

    public void setHour(String hour)
    {
        this.hour = hour;
    }

    public String getHour()
    {
        return hour;
    }
}
