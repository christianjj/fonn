package com.fonn.link.modal;

public class image_details {


    private String Image;
    private String Date;
    private String filesize;
    private String Id;
    private String path;
    private String callurl;

    public image_details(String image, String date, String filesize, String id, String path, String callurl) {
        Image = image;
        Date = date;
        this.filesize = filesize;
        Id = id;
        this.path = path;
        this.callurl = callurl;
    }


    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getFilesize() {
        return filesize;
    }

    public void setFilesize(String filesize) {
        this.filesize = filesize;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public String getCallurl() {
        return callurl;
    }

    public void setCallurl(String callurl) {
        this.callurl = callurl;
    }
}
