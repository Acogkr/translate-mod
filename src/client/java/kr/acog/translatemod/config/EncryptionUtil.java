package kr.acog.translatemod.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger("translatemod");

    private static Cipher initCipher(int mode) throws Exception {
        SecretKey key = LocalKeyManager.getSecretKey();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, key);
        return cipher;
    }

    public static String encrypt(String strToEncrypt) {
        if (strToEncrypt == null || strToEncrypt.isEmpty()) {
            return strToEncrypt;
        }
        try {
            return Base64.getEncoder().encodeToString(
                    initCipher(Cipher.ENCRYPT_MODE).doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            LOGGER.error("암호화 실패", e);
        }
        return null;
    }

    public static String decrypt(String strToDecrypt) {
        if (strToDecrypt == null || strToDecrypt.isEmpty()) {
            return strToDecrypt;
        }
        try {
            return new String(
                    initCipher(Cipher.DECRYPT_MODE).doFinal(Base64.getDecoder().decode(strToDecrypt)),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("복호화 실패", e);
        }
        return null;
    }
}
