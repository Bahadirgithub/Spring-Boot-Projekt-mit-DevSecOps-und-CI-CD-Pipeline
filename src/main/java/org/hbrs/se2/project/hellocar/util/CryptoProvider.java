package org.hbrs.se2.project.hellocar.util;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CryptoProvider {

    private static PasswordEncoder Encoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 65536, 3);
    }

    /**
     *
     * @param password
     * @return gehashtes passwort
     */
    public static String CreatePassword(String password) {
        return Encoder().encode(password);
    }

    /**
     *
     * @param password User input password
     * @param pPassword Database Password
     *
     * @return
     */
    public static boolean ValidatePassword(String password, String pPassword) {
        return ValidatePassword(Encoder(), password, pPassword);
    }

    /**
     *
     * @param encoder
     * @param password
     * @param pPassword
     *
     * @return
     */
    public static boolean ValidatePassword(PasswordEncoder encoder, String password, String pPassword) {
        return encoder.matches(password, pPassword);
    }
}
