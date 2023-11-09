package com.bns.ts.fpe;

public class Utils {

  // luhn algorithm
  // see https://www.baeldung.com/java-validate-cc-number
  public static int getCheckDigit(String cardNumber) {
    int sum = 0;
    for (int i = cardNumber.length() - 1; i >= 0; i--) {
      int digit = Integer.parseInt(cardNumber.substring(i, i + 1));
      if ((cardNumber.length() - i - 1) % 2 == 0) {
        digit = doubleAndSumDigits(digit);
      }

      sum += digit;
    }
    return 10 - sum % 10;
  }

  private static int doubleAndSumDigits(int digit) {
    int ret = digit * 2;

    if (ret > 9) {
      ret -= 9;
    }
    return ret;
  }
}
