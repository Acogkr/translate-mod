package kr.acog.translatemod.config;

import net.fabricmc.loader.api.FabricLoader;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public class LocalKeyManager {

    private static final Path KEY_PATH = FabricLoader.getInstance().getConfigDir().resolve("translatemod.key");
    private static SecretKey cachedKey;

    public static SecretKey getSecretKey() {
        if (cachedKey != null) {
            return cachedKey;
        }

        try {
            if (Files.exists(KEY_PATH)) {
                byte[] keyBytes = Files.readAllBytes(KEY_PATH);
                cachedKey = new SecretKeySpec(keyBytes, "AES");
            } else {
                byte[] keyBytes = new byte[16];
                SecureRandom random = new SecureRandom();
                random.nextBytes(keyBytes);
                
                Files.write(KEY_PATH, keyBytes);
                cachedKey = new SecretKeySpec(keyBytes, "AES");
            }
        } catch (IOException e) {
            throw new RuntimeException("암호화 키를 관리하는 도중 오류가 발생했습니다.", e);
        }

        return cachedKey;
    }
}