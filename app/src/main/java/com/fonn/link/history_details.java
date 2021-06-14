package com.fonn.link;

public class history_details {

    private String Myname;
    private String Date;
    private String Duration;


    public history_details(String name, String date, String duration) {
        Myname = name;
        Date = date;
        Duration = duration;
    }

    public history_details() {
    }

    public String getMyname() {
        return Myname;
    }

    public void setName(String name) {
        Myname = name;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getDuration() {
        return Duration;
    }

    public void setDuration(String duration) {
        Duration = duration;
    }


}
