package org.hbrs.se2.project.hellocar.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TestCryptoProvider {

    @Test
    public void testPasswordCreation() {
        String hash = CryptoProvider.CreatePassword("Test1234");
        System.out.println(hash);
    }

    @Test
    public void testPasswordValidation() {
        boolean validate = CryptoProvider.ValidatePassword("Test1234", "$argon2id$v=19$m=65536,t=3,p=1$j/nWgJlU3UqrRdRtXXvT3w$tn3C3UMtTNxV5nN+mc84cQp5fjzC4RrlvaFXAxRtkOI");
        assertTrue(validate);
    }
}
