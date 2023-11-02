package com.bns.ts.fpe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class Tokens {

  private final List<String> tokens;
  private final int[] layout;
  private final String delimiter;

  public Tokens(String content, String delimiter, boolean hasCheckDigit) {
    this.delimiter = delimiter;
    if (hasCheckDigit) {
      content = content.substring(0, content.length() - 1);
    }
    tokens =
        Collections.list(new StringTokenizer(content, delimiter)).stream()
            .map(token -> (String) token)
            .collect(Collectors.toList());

    Integer[] offsets =
        tokens.stream().map(token -> token.length()).toArray(Integer[]::new);

    layout = new int[offsets.length];
    for (int i = 0; i < layout.length; i++) {
      layout[i] = offsets[i];
    }
  }

  public Tokens(String content, String delimiter, int[] layout) {
    this.delimiter = delimiter;
    tokens = new ArrayList<>();
    for (int i = 0, begin = 0, end = 0; i < layout.length; i++) {
      end += layout[i];
      tokens.add(content.substring(begin, end));
      begin = end;
    }
    this.layout = layout;
  }

  public int[] getLayout() {
    return layout;
  }

  public String stripped() {
    return String.join("", tokens);
  }

  public String assembled(boolean checkDigitRequired) {
    String delimited = String.join(delimiter, tokens);
    return checkDigitRequired ?
        delimited + Utils.getCheckDigit(stripped())
        : delimited;
  }

  public String getDelimiter() {
    return delimiter;
  }
}
