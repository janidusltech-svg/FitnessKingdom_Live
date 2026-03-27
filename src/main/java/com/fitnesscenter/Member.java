package com.fitnesscenter;

public class Member {
    private String id;
    private String name;
    private String email;
    private String age;
    private String phone;
    private String nic;
    private String membershipPlan;
    private String duration;
    private String status;
    private String registeredDate;

    // Temporary variables used for the Search/Billing page
    private int planPrice;
    private int totalPaid;
    private int balance;

    // The perfectly matched 10-field constructor
    public Member(String id, String name, String email, String age, String phone, String nic, String membershipPlan, String duration, String status, String registeredDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.phone = phone;
        this.nic = nic;
        this.membershipPlan = membershipPlan;
        this.duration = duration;
        this.status = status;
        this.registeredDate = registeredDate;
    }

    // --- GETTERS & SETTERS (These prevent the 500 HTML Error!) ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public String getMembershipPlan() { return membershipPlan; }
    public void setMembershipPlan(String membershipPlan) { this.membershipPlan = membershipPlan; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRegisteredDate() { return registeredDate; }
    public void setRegisteredDate(String registeredDate) { this.registeredDate = registeredDate; }

    public int getPlanPrice() { return planPrice; }
    public void setPlanPrice(int planPrice) { this.planPrice = planPrice; }

    public int getTotalPaid() { return totalPaid; }
    public void setTotalPaid(int totalPaid) { this.totalPaid = totalPaid; }

    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }

    // Used by FileHandler to save the data properly
    public String toFileString() {
        return id + "," + name + "," + email + "," + age + "," + phone + "," + nic + "," + membershipPlan + "," + duration + "," + status + "," + registeredDate;
    }
}