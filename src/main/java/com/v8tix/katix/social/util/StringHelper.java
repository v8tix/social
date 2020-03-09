package com.v8tix.katix.social.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface StringHelper {

  Logger LOGGER = LoggerFactory.getLogger(StringHelper.class);
  String EMPTY_STRING = "";
  String COLON = ":";
  String COMA = ",";

  static String removeLastChar(String str, final char character) {
    if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == character) {
      str = str.substring(0, str.length() - 1);
    }
    return str;
  }

  static String concatStrings(final String delimiter, final String init, final String... last) {
    assert delimiter != null;
    final StringBuilder stringBuilder = new StringBuilder();
    if (init != null && !init.isEmpty()) {
      stringBuilder.append(init).append(delimiter);
    }
    for (String s : last) {
      if (!s.isEmpty()) {
        stringBuilder.append(s).append(delimiter);
      }
    }
    if (delimiter.isEmpty()) {
      return stringBuilder.toString();
    } else {
      return removeLastChar(stringBuilder.toString(), delimiter.charAt(0));
    }
  }

  static String toJson(final Object value) {
    try {
      return new ObjectMapper().writeValueAsString(value);
    } catch (JsonProcessingException e) {
      return EMPTY_STRING;
    }
  }
}
