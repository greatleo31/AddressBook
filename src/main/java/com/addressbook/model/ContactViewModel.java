package com.addressbook.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import com.addressbook.utils.PinyinUtil;

public class ContactViewModel {
  private final StringProperty id;
  private final StringProperty name;
  private final StringProperty phone;
  private final StringProperty mobile;
  private final StringProperty imAccount;
  private final StringProperty email;
  private final StringProperty homepage;
  private final StringProperty birthday;
  private final StringProperty photoBase64;
  private final StringProperty company;
  private final StringProperty address;
  private final StringProperty zipcode;
  private final StringProperty group;
  private final StringProperty remark;

  // 用于搜索缓存
  private final StringProperty fullPinyin = new SimpleStringProperty("");
  private final StringProperty firstLetter = new SimpleStringProperty("");

  public ContactViewModel(Contact contact) {
    this.id = new SimpleStringProperty(contact.getId());
    this.name = new SimpleStringProperty(contact.getName() == null ? "" : contact.getName());
    this.phone = new SimpleStringProperty(contact.getPhone() == null ? "" : contact.getPhone());
    this.mobile = new SimpleStringProperty(contact.getMobile() == null ? "" : contact.getMobile());
    this.imAccount = new SimpleStringProperty(contact.getImAccount() == null ? "" : contact.getImAccount());
    this.email = new SimpleStringProperty(contact.getEmail() == null ? "" : contact.getEmail());
    this.homepage = new SimpleStringProperty(contact.getHomepage() == null ? "" : contact.getHomepage());
    this.birthday = new SimpleStringProperty(contact.getBirthday() == null ? "" : contact.getBirthday());
    this.photoBase64 = new SimpleStringProperty(contact.getPhotoBase64() == null ? "" : contact.getPhotoBase64());
    this.company = new SimpleStringProperty(contact.getCompany() == null ? "" : contact.getCompany());
    this.address = new SimpleStringProperty(contact.getAddress() == null ? "" : contact.getAddress());
    this.zipcode = new SimpleStringProperty(contact.getZipcode() == null ? "" : contact.getZipcode());
    this.group = new SimpleStringProperty(contact.getGroup() == null ? "" : contact.getGroup());
    this.remark = new SimpleStringProperty(contact.getRemark() == null ? "" : contact.getRemark());

    updatePinyinCache();
  }

  public Contact toContact() {
    Contact contact = new Contact();
    contact.setId(id.get());
    contact.setName(name.get());
    contact.setPhone(phone.get());
    contact.setMobile(mobile.get());
    contact.setImAccount(imAccount.get());
    contact.setEmail(email.get());
    contact.setHomepage(homepage.get());
    contact.setBirthday(birthday.get());
    contact.setPhotoBase64(photoBase64.get());
    contact.setCompany(company.get());
    contact.setAddress(address.get());
    contact.setZipcode(zipcode.get());
    contact.setGroup(group.get());
    contact.setRemark(remark.get());
    return contact;
  }

  public void updateFrom(Contact contact) {
    this.id.set(contact.getId());
    this.name.set(contact.getName() == null ? "" : contact.getName());
    this.phone.set(contact.getPhone() == null ? "" : contact.getPhone());
    this.mobile.set(contact.getMobile() == null ? "" : contact.getMobile());
    this.imAccount.set(contact.getImAccount() == null ? "" : contact.getImAccount());
    this.email.set(contact.getEmail() == null ? "" : contact.getEmail());
    this.homepage.set(contact.getHomepage() == null ? "" : contact.getHomepage());
    this.birthday.set(contact.getBirthday() == null ? "" : contact.getBirthday());
    this.photoBase64.set(contact.getPhotoBase64() == null ? "" : contact.getPhotoBase64());
    this.company.set(contact.getCompany() == null ? "" : contact.getCompany());
    this.address.set(contact.getAddress() == null ? "" : contact.getAddress());
    this.zipcode.set(contact.getZipcode() == null ? "" : contact.getZipcode());
    this.group.set(contact.getGroup() == null ? "" : contact.getGroup());
    this.remark.set(contact.getRemark() == null ? "" : contact.getRemark());

    updatePinyinCache();
  }

  public void updatePinyinCache() {
    if (this.name.get() != null && !this.name.get().isEmpty()) {
      this.fullPinyin.set(PinyinUtil.getFullPinyin(this.name.get()));
      this.firstLetter.set(PinyinUtil.getFirstLetter(this.name.get()));
    } else {
      this.fullPinyin.set("");
      this.firstLetter.set("");
    }
  }

  public String getFullPinyin() {
    return fullPinyin.get();
  }

  public String getFirstLetter() {
    return firstLetter.get();
  }

  // Properties... (I will only generate standard getters since
  // PropertyValueFactory uses `xxxProperty()`)

  public StringProperty idProperty() {
    return id;
  }

  public String getId() {
    return id.get();
  }

  public void setId(String value) {
    id.set(value);
  }

  public StringProperty nameProperty() {
    return name;
  }

  public String getName() {
    return name.get();
  }

  public void setName(String value) {
    name.set(value);
  }

  public StringProperty phoneProperty() {
    return phone;
  }

  public String getPhone() {
    return phone.get();
  }

  public void setPhone(String value) {
    phone.set(value);
  }

  public StringProperty mobileProperty() {
    return mobile;
  }

  public String getMobile() {
    return mobile.get();
  }

  public void setMobile(String value) {
    mobile.set(value);
  }

  public StringProperty imAccountProperty() {
    return imAccount;
  }

  public String getImAccount() {
    return imAccount.get();
  }

  public void setImAccount(String value) {
    imAccount.set(value);
  }

  public StringProperty emailProperty() {
    return email;
  }

  public String getEmail() {
    return email.get();
  }

  public void setEmail(String value) {
    email.set(value);
  }

  public StringProperty homepageProperty() {
    return homepage;
  }

  public String getHomepage() {
    return homepage.get();
  }

  public void setHomepage(String value) {
    homepage.set(value);
  }

  public StringProperty birthdayProperty() {
    return birthday;
  }

  public String getBirthday() {
    return birthday.get();
  }

  public void setBirthday(String value) {
    birthday.set(value);
  }

  public StringProperty photoBase64Property() {
    return photoBase64;
  }

  public String getPhotoBase64() {
    return photoBase64.get();
  }

  public void setPhotoBase64(String value) {
    photoBase64.set(value);
  }

  public StringProperty companyProperty() {
    return company;
  }

  public String getCompany() {
    return company.get();
  }

  public void setCompany(String value) {
    company.set(value);
  }

  public StringProperty addressProperty() {
    return address;
  }

  public String getAddress() {
    return address.get();
  }

  public void setAddress(String value) {
    address.set(value);
  }

  public StringProperty zipcodeProperty() {
    return zipcode;
  }

  public String getZipcode() {
    return zipcode.get();
  }

  public void setZipcode(String value) {
    zipcode.set(value);
  }

  public StringProperty groupProperty() {
    return group;
  }

  public String getGroup() {
    return group.get();
  }

  public void setGroup(String value) {
    group.set(value);
  }

  public StringProperty remarkProperty() {
    return remark;
  }

  public String getRemark() {
    return remark.get();
  }

  public void setRemark(String value) {
    remark.set(value);
  }
}
