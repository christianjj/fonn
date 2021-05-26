package com.fonn.link;

public class profile_details {

    private String Name;
    private String Balance;
    private String Number;
    private String Status;

    public profile_details(String name, String balance, String number, String status) {
        Name = name;
        Balance = balance;
        Number = number;
        Status = status;
    }

    public profile_details() {
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getBalance() {
        return Balance;
    }

    public void setBalance(String balance) {
        Balance = balance;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }
}
