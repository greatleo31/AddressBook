package com.addressbook.utils;

import com.addressbook.model.Contact;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ContactImportExportUtil {

  // ---------------- CSV 导入导出 ----------------

  public static void exportToCsv(List<Contact> contacts, File file) throws IOException {
    try (BufferedWriter bw = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
      // 写入 BOM 避免 Excel 乱码
      bw.write('\ufeff');
      // 表头
      bw.write("姓名,电话,手机,通讯工具,电子邮件,个人主页,生日,工作单位,家庭地址,邮政编码,分组,备注,照片流(Base64)\n");
      for (Contact c : contacts) {
        bw.write(escapeCsv(c.getName()) + ",");
        bw.write(escapeCsv(c.getPhone()) + ",");
        bw.write(escapeCsv(c.getMobile()) + ",");
        bw.write(escapeCsv(c.getImAccount()) + ",");
        bw.write(escapeCsv(c.getEmail()) + ",");
        bw.write(escapeCsv(c.getHomepage()) + ",");
        bw.write(escapeCsv(c.getBirthday()) + ",");
        bw.write(escapeCsv(c.getCompany()) + ",");
        bw.write(escapeCsv(c.getAddress()) + ",");
        bw.write(escapeCsv(c.getZipcode()) + ",");
        bw.write(escapeCsv(c.getGroup()) + ",");
        bw.write(escapeCsv(c.getRemark()) + ",");
        bw.write(escapeCsv(c.getPhotoBase64()) + "\n");
      }
    }
  }

  public static List<Contact> importFromCsv(File file) throws IOException {
    List<Contact> contacts = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
      String line = br.readLine();
      // 简单处理 BOM
      if (line != null && line.startsWith("\ufeff")) {
        line = line.substring(1);
      }
      // 忽略表头如果它匹配预期
      if (line != null && line.contains("姓名,电话")) {
        line = br.readLine();
      }

      while (line != null) {
        if (line.trim().isEmpty()) {
          line = br.readLine();
          continue;
        }
        // 简单的逗号分割（暂不处理复杂内嵌逗号换行，满足"能跑就行"原则，或者简单拆分）
        String[] parts = line.split(",", -1);
        if (parts.length >= 1) {
          Contact c = new Contact();
          c.setName(unquote(parts[0]));
          if (parts.length > 1)
            c.setPhone(unquote(parts[1]));
          if (parts.length > 2)
            c.setMobile(unquote(parts[2]));
          if (parts.length > 3)
            c.setImAccount(unquote(parts[3]));
          if (parts.length > 4)
            c.setEmail(unquote(parts[4]));
          if (parts.length > 5)
            c.setHomepage(unquote(parts[5]));
          if (parts.length > 6)
            c.setBirthday(unquote(parts[6]));
          if (parts.length > 7)
            c.setCompany(unquote(parts[7]));
          if (parts.length > 8)
            c.setAddress(unquote(parts[8]));
          if (parts.length > 9)
            c.setZipcode(unquote(parts[9]));
          if (parts.length > 10)
            c.setGroup(unquote(parts[10]));
          if (parts.length > 11)
            c.setRemark(unquote(parts[11]));
          if (parts.length > 12)
            c.setPhotoBase64(unquote(parts[12]));

          if (c.getName() != null && !c.getName().trim().isEmpty()) {
            contacts.add(c);
          }
        }
        line = br.readLine();
      }
    }
    return contacts;
  }

  private static String escapeCsv(String val) {
    if (val == null)
      return "";
    val = val.replace("\"", "\"\"");
    if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
      return "\"" + val + "\"";
    }
    return val;
  }

  private static String unquote(String val) {
    if (val == null)
      return "";
    if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
      val = val.substring(1, val.length() - 1);
    }
    return val.replace("\"\"", "\"").trim();
  }

  // ---------------- vCard 导入导出 ----------------

  public static void exportToVCard(List<Contact> contacts, File file) throws IOException {
    try (BufferedWriter bw = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
      for (Contact c : contacts) {
        bw.write("BEGIN:VCARD\n");
        bw.write("VERSION:3.0\n");
        if (c.getName() != null)
          bw.write("FN:" + c.getName() + "\n");
        if (c.getPhone() != null && !c.getPhone().isEmpty())
          bw.write("TEL;TYPE=HOME:" + c.getPhone() + "\n");
        if (c.getMobile() != null && !c.getMobile().isEmpty())
          bw.write("TEL;TYPE=CELL:" + c.getMobile() + "\n");
        if (c.getEmail() != null && !c.getEmail().isEmpty())
          bw.write("EMAIL:" + c.getEmail() + "\n");
        if (c.getHomepage() != null && !c.getHomepage().isEmpty())
          bw.write("URL:" + c.getHomepage() + "\n");
        if (c.getBirthday() != null && !c.getBirthday().isEmpty())
          bw.write("BDAY:" + c.getBirthday() + "\n");
        if (c.getCompany() != null && !c.getCompany().isEmpty())
          bw.write("ORG:" + c.getCompany() + "\n");
        if (c.getRemark() != null && !c.getRemark().isEmpty())
          bw.write("NOTE:" + c.getRemark() + "\n");
        if (c.getGroup() != null && !c.getGroup().isEmpty())
          bw.write("CATEGORIES:" + c.getGroup() + "\n");
        // 暂时利用 X-IM 等自定义字段存储 IM
        if (c.getImAccount() != null && !c.getImAccount().isEmpty())
          bw.write("X-IM:" + c.getImAccount() + "\n");
        if (c.getZipcode() != null && !c.getZipcode().isEmpty())
          bw.write("X-ZIPCODE:" + c.getZipcode() + "\n");
        if (c.getAddress() != null && !c.getAddress().isEmpty()) {
          bw.write("ADR:;;;" + c.getAddress() + ";;;;\n");
        }
        if (c.getPhotoBase64() != null && !c.getPhotoBase64().isEmpty()) {
          bw.write("PHOTO;ENCODING=b;TYPE=PNG:" + c.getPhotoBase64() + "\n");
        }
        bw.write("END:VCARD\n");
      }
    }
  }

  public static List<Contact> importFromVCard(File file) throws IOException {
    List<Contact> contacts = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
      String line;
      Contact curr = null;
      StringBuilder photoSb = null;
      boolean readingPhoto = false;

      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty())
          continue;

        if (line.startsWith("BEGIN:VCARD")) {
          curr = new Contact();
          continue;
        }
        if (line.startsWith("END:VCARD") && curr != null) {
          if (curr.getName() != null && !curr.getName().trim().isEmpty()) {
            contacts.add(curr);
          }
          curr = null;
          readingPhoto = false;
          photoSb = null;
          continue;
        }

        if (curr == null)
          continue;

        if (readingPhoto && !line.contains(":")) {
          // base64可能换行
          photoSb.append(line);
          continue;
        } else if (readingPhoto) {
          // 遇到新的字段，结束photo读取
          curr.setPhotoBase64(photoSb.toString());
          readingPhoto = false;
        }

        if (line.startsWith("FN:")) {
          curr.setName(line.substring(3).trim());
        } else if (line.startsWith("TEL;TYPE=HOME:") || line.startsWith("TEL;TYPE=voice,home:")) {
          curr.setPhone(line.substring(line.indexOf(":") + 1).trim());
        } else if (line.startsWith("TEL;TYPE=CELL:") || line.startsWith("TEL;TYPE=voice,cell:")) {
          curr.setMobile(line.substring(line.indexOf(":") + 1).trim());
        } else if (line.startsWith("EMAIL:")) {
          curr.setEmail(line.substring(6).trim());
        } else if (line.startsWith("URL:")) {
          curr.setHomepage(line.substring(4).trim());
        } else if (line.startsWith("BDAY:")) {
          curr.setBirthday(line.substring(5).trim());
        } else if (line.startsWith("ORG:")) {
          curr.setCompany(line.substring(4).trim());
        } else if (line.startsWith("NOTE:")) {
          curr.setRemark(line.substring(5).trim());
        } else if (line.startsWith("CATEGORIES:")) {
          curr.setGroup(line.substring(11).trim());
        } else if (line.startsWith("X-IM:")) {
          curr.setImAccount(line.substring(5).trim());
        } else if (line.startsWith("X-ZIPCODE:")) {
          curr.setZipcode(line.substring(10).trim());
        } else if (line.startsWith("ADR:")) {
          String adr = line.substring(line.indexOf(":") + 1);
          String[] parts = adr.split(";", -1);
          if (parts.length > 3 && !parts[3].isEmpty()) {
            curr.setAddress(parts[3]);
          } else if (parts.length > 2 && !parts[2].isEmpty()) {
            curr.setAddress(parts[2]);
          } else if (parts.length > 0) {
            curr.setAddress(parts[0]);
          }
        } else if (line.startsWith("PHOTO;")) {
          readingPhoto = true;
          photoSb = new StringBuilder();
          photoSb.append(line.substring(line.indexOf(":") + 1).trim());
        }
      }
      if (curr != null && curr.getName() != null && !curr.getName().isEmpty()) {
        if (readingPhoto && photoSb != null) {
          curr.setPhotoBase64(photoSb.toString());
        }
        contacts.add(curr);
      }
    }
    return contacts;
  }
}
