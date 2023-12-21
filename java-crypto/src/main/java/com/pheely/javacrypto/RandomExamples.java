package com.pheely.javacrypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Random;

public class RandomExamples implements CommandLineRunner {
    private Logger logger = LoggerFactory.getLogger(RandomExamples.class);

    @Override
    public void run(String... args) throws NoSuchAlgorithmException {
        Random random = new Random();
        SecureRandom secureRandom = SecureRandom.getInstance("NativePRNG");
        // to mimic credit card number
        // Using java.util.Random
        // no repeated numbers found among 10,000,000 generated numbers
        // However, 6 duplicates found among 100,000,000 generated numbers
        // select count(*), num from load_data group by num having count(*) > 1;
//        count |       num
//       -------+-----------------
//        2 | 401239061159393
//        2 | 515953930493411
//        2 | 819454957766118
//        2 | 822581986389944
//        2 | 861762681330496
//        2 | 881439532723525
        // Using secureRandom, 5 duplicates found 
        // command: java -jar build/libs/java-crypto-1.0.jar |grep 'Random integer' | awk '{print $15}'|sort|uniq -d
        for (long i = 0; i < 100000000l; i++) {
//            random.nextLong(100000000000000l, 999999999999999l);
            logger.info("Random integer between 1000-0000-0000-000 and 9999-9999-9999-999: {}", secureRandom.nextLong(100000000000000l, 999999999999999l));
        }
    }
}
