package com.pheely.javacrypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureRandomExample implements CommandLineRunner {
    private Logger logger = LoggerFactory.getLogger(SecureRandomExample.class);
    @Override
    public void run(String... args) {
        generateSecureRandomValues();
    }

    private void generateSecureRandomValues() {
        try {
            SecureRandom random = getSecureRandomForAlgorithm("NativePRNG");
//            SecureRandom random = new SecureRandom();
//            random.setSeed(getSecureRanddomSeed());

            logger.info("random integer: {}", random.nextInt());
            byte[] bytes = new byte[124];
            random.nextBytes(bytes);
            logger.info("random bytes: {}", Base64.getEncoder().encode(bytes));
        } catch (Exception ex) {
            logger.error("Caught exception", ex);
        }
    }

    private SecureRandom getSecureRandomForAlgorithm(String algorithm) throws NoSuchAlgorithmException {
        if (algorithm == null || algorithm.isEmpty()) {
            return new SecureRandom();
        }

        return SecureRandom.getInstance(algorithm);
    }

    private byte[] getSecureRanddomSeed() {
        return SecureRandom.getSeed(256);
    }
}
