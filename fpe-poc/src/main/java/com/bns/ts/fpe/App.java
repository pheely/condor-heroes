package com.bns.ts.fpe;

import com.privacylogistics.FF3Cipher;

public class App {
    public static void main(String[] args) throws Exception {
        FF3Cipher c = new FF3Cipher("2DE79D232DF5585D68CE47882AE256D6", "CBD09280979564");
    	String pt = "399-252-0240";
    	String ciphertext = c.encrypt(pt);
    	String plaintext = c.decrypt(ciphertext);
	System.out.println("pt = " + pt + ", ciphertext = " + ciphertext + ", plaintext = " + plaintext);
    }
}
