package com.addressbook.service;

import com.addressbook.model.Contact;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataStorageService {

  private static final String DATA_FILE = "contacts_data.dat";

  /**
   * 将联系人列表序列化保存到本地文件
   *
   * @param contacts 联系人列表
   */
  public void saveContacts(List<Contact> contacts) {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
      oos.writeObject(contacts);
      System.out.println("成功保存 " + contacts.size() + " 个联系人数据到文件。");
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("保存联系人数据时发生错误: " + e.getMessage());
    }
  }

  /**
   * 从本地文件反序列化加载联系人列表
   *
   * @return 联系人列表，如果文件不存在或读取失败则返回空列表
   */
  @SuppressWarnings("unchecked")
  public List<Contact> loadContacts() {
    File file = new File(DATA_FILE);
    if (!file.exists()) {
      System.out.println("数据文件不存在，返回空列表。");
      return new ArrayList<>();
    }

    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
      Object obj = ois.readObject();
      if (obj instanceof List) {
        System.out.println("成功从文件加载联系人数据。");
        return (List<Contact>) obj;
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      System.err.println("加载联系人数据时发生错误: " + e.getMessage());
    }
    return new ArrayList<>();
  }
}
