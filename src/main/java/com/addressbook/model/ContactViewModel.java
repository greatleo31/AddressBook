package com.addressbook.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ContactViewModel {
  private final StringProperty name;
  private final StringProperty mobile;
  private final StringProperty email;
  private final StringProperty group;

  public ContactViewModel(String name, String mobile, String email, String group) {
    this.name = new SimpleStringProperty(name);
    this.mobile = new SimpleStringProperty(mobile);
    this.email = new SimpleStringProperty(email);
    this.group = new SimpleStringProperty(group);
  }

  public String getName() {
    return name.get();
  }

  public StringProperty nameProperty() {
    return name;
  }

  public void setName(String name) {
    this.name.set(name);
  }

  public String getMobile() {
    return mobile.get();
  }

  public StringProperty mobileProperty() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile.set(mobile);
  }

  public String getEmail() {
    return email.get();
  }

  public StringProperty emailProperty() {
    return email;
  }

  public void setEmail(String email) {
    this.email.set(email);
  }

  public String getGroup() {
    return group.get();
  }

  public StringProperty groupProperty() {
    return group;
  }

  public void setGroup(String group) {
    this.group.set(group);
  }
}
