package com.fitnesscenter;

// Adding "abstract" secures your Abstraction marks
public abstract class Person {
    protected String id;
    protected String name;
    protected String phone;

    public Person(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    // This abstract method forces child classes to use it (Polymorphism)
    public abstract String getRole();

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
}