package com.addressbook.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinUtil {

  private static final HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();

  static {
    defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
  }

  /**
   * 获取字符串的完整拼音（小写，剔除空格和特殊字符）
   */
  public static String getFullPinyin(String chinese) {
    if (chinese == null || chinese.trim().isEmpty()) {
      return "";
    }
    StringBuilder pinyinStr = new StringBuilder();
    char[] charArray = chinese.toCharArray();
    for (char c : charArray) {
      if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
        try {
          String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat);
          if (pinyinArray != null && pinyinArray.length > 0) {
            pinyinStr.append(pinyinArray[0]);
          }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
          e.printStackTrace();
        }
      } else {
        pinyinStr.append(Character.toLowerCase(c));
      }
    }
    return pinyinStr.toString();
  }

  /**
   * 获取字符串的拼音首字母（小写）
   */
  public static String getFirstLetter(String chinese) {
    if (chinese == null || chinese.trim().isEmpty()) {
      return "";
    }
    StringBuilder pinyinStr = new StringBuilder();
    char[] charArray = chinese.toCharArray();
    for (char c : charArray) {
      if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
        try {
          String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat);
          if (pinyinArray != null && pinyinArray.length > 0) {
            pinyinStr.append(pinyinArray[0].charAt(0));
          }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
          e.printStackTrace();
        }
      } else {
        pinyinStr.append(Character.toLowerCase(c));
      }
    }
    return pinyinStr.toString();
  }
}
