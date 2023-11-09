package com.bns.ts.fpe;

import com.google.common.io.BaseEncoding;
import com.privacylogistics.FF3Cipher;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Scanner;
import javax.crypto.spec.SecretKeySpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class App {

//  static final Logger logger = LogManager.getLogger();

  public static void main(String[] args) throws Exception {
    String type = "";
    String numberRegex = "(\\d|-)+";
    String alphabetRegex = "[a-z A-Z]+";
    String content = "";
    String checkDigit = "";

    Scanner scan = new Scanner(System.in);
    while (true) {
      if (!type.equalsIgnoreCase("N") && !type.equalsIgnoreCase("S")) {
        System.out.print("\nWhich data type: N(umber) | S(tring): ");
        if (!scan.hasNextLine()) {
          break;
        }
        type = scan.nextLine();
      }

      if (content.length() == 0
          && ((type.equalsIgnoreCase("N") && !content.matches(numberRegex))
          || (type.equalsIgnoreCase("S") && !content.matches(alphabetRegex)))) {
        System.out.print("Enter the plaintext: ");
        content = scan.nextLine();
      }

      if (type.equalsIgnoreCase("N")
          && (!checkDigit.equalsIgnoreCase("Y")
          && !checkDigit.equalsIgnoreCase("N"))) {
        System.out.print("Does it contain a checkdigit (Y|N)? ");
        checkDigit = scan.nextLine();
      }

      switch (type) {
        case "N":
        case "n":
          testNumber(content, checkDigit.equals("Y"));
          break;
        case "S":
        case "s":
          testAlphabet(content);
          break;
      }
//    testNumber("399*252-0240", false);
//    testNumber("5457-6238-9823-4113", true);
//    testAlphabet("Philip Yang");
      type = "";
      content = "";
      checkDigit = "";
    }
    scan.close();
  }

  private static void testNumber(String pt, boolean checkDigit) throws Exception {
    String key = getSecureRandomKey("AES", 128);
    String tweak = getTweak(7);

//    FF3Cipher c = new FF3Cipher("2DE79D232DF5585D68CE47882AE256D6", "CBD09280979564");
    FF3Cipher c = new FF3Cipher(key, tweak, "0123456789");
    Tokens pt_tokens = new Tokens(pt, "-", checkDigit);

    String ciphertext = c.encrypt(pt_tokens.stripped());
    Tokens ct_tokens = new Tokens(ciphertext, pt_tokens.getDelimiter(),
        pt_tokens.getLayout());

    String plaintext = c.decrypt(ciphertext);
    Tokens plaintext_tokens = new Tokens(plaintext, pt_tokens.getDelimiter(),
        pt_tokens.getLayout());

    System.out.println("plaintext  = " + pt + "\nciphertext = " + ct_tokens.assembled(checkDigit));
  }

  private static void testAlphabet(String pt) throws Exception {
    String key = getSecureRandomKey("AES", 128);
    String tweak = getTweak(7);

//    FF3Cipher c = new FF3Cipher("2DE79D232DF5585D68CE47882AE256D6", "CBD09280979564");
    FF3Cipher c = new FF3Cipher(key, tweak,
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
    Tokens pt_tokens = new Tokens(pt, " ", false);

    String ciphertext = c.encrypt(pt_tokens.stripped());
    Tokens ct_tokens = new Tokens(ciphertext, pt_tokens.getDelimiter(),
        pt_tokens.getLayout());

    String plaintext = c.decrypt(ciphertext);
    Tokens plaintext_tokens = new Tokens(plaintext, pt_tokens.getDelimiter(),
        pt_tokens.getLayout());

    System.out.println("plaintext  = " + pt + "\nciphertext = " + ct_tokens.assembled(false));
  }
  // key length must be 128, 192, or 256
  private static String getSecureRandomKey(String cipher, int keySize) {
    byte[] secureRandomKeyBytes = new byte[keySize/8];
    SecureRandom secureRandom = new SecureRandom();
    secureRandom.nextBytes(secureRandomKeyBytes);
    Key key = new SecretKeySpec(secureRandomKeyBytes, cipher);
    return BaseEncoding.base16().encode(key.getEncoded());
  }

  // The tweak is 7 bytes for FF3-1 or 8 bytes for FF3.
  // It is not generally kept secret.
  private static String getTweak(int size) {
    byte[] tweak = new byte[size];
    SecureRandom secureRandom = new SecureRandom();
    secureRandom.nextBytes(tweak);
    return BaseEncoding.base16().encode(tweak);
  }
}