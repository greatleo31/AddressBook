package com.addressbook.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Contact implements Serializable {
  private static final long serialVersionUID = 1L;

  private String id;
  private String name;
  private String phone;
  private String mobile;
  private String imAccount; // 即时通讯工具及号码 (如 QQ:12345, WeChat:abc)
  private String email;
  private String homepage;
  private String birthday;
  private String photoBase64;
  private String company;
  private String address;
  private String zipcode;
  private String group;
  private String remark;

  public Contact() {
    this.id = UUID.randomUUID().toString();
    this.group = "未分组联系人"; // 默认分组
  }

  // Getters and Setters

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getImAccount() {
    return imAccount;
  }

  public void setImAccount(String imAccount) {
    this.imAccount = imAccount;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getHomepage() {
    return homepage;
  }

  public void setHomepage(String homepage) {
    this.homepage = homepage;
  }

  public String getBirthday() {
    return birthday;
  }

  public void setBirthday(String birthday) {
    this.birthday = birthday;
  }

  public String getPhotoBase64() {
    return photoBase64;
  }

  public void setPhotoBase64(String photoBase64) {
    this.photoBase64 = photoBase64;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getZipcode() {
    return zipcode;
  }

  public void setZipcode(String zipcode) {
    this.zipcode = zipcode;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Contact contact = (Contact) o;
    return Objects.equals(id, contact.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
