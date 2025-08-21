package ru.stm.shcherbinki3.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.stm.shcherbinki3.dto.jwt.JwtToken;
import ru.stm.shcherbinki3.model.redis.AccountInfo;
import ru.stm.shcherbinki3.util.exception.BadRequestException;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class JwtRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public JwtRedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(JwtToken jwtToken) {
        return jwtToken.id() + "::" + jwtToken.jti();
    }

    public void signIn(JwtToken jwtToken) {
        // Устанавливаем TTL до времени истечения токена
        long ttlSeconds = jwtToken.expiresAt().getEpochSecond() - Instant.now().getEpochSecond();
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(buildKey(jwtToken), "active", ttlSeconds, TimeUnit.SECONDS);
        }
    }

    public void logout(JwtToken jwtToken) {
        redisTemplate.delete(buildKey(jwtToken));
    }

    public boolean checkForAccess(JwtToken jwtToken) {
        return redisTemplate.hasKey(buildKey(jwtToken));
    }

    public List<AccountInfo> getActiveAccounts(JwtToken jwtToken) {
        String pattern = jwtToken.id() + "::*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        return keys.stream()
                .map(k -> {
                    Long userId = jwtToken.id();
                    String jti = k.split("::")[1];

                    Long ttlSeconds = redisTemplate.getExpire(k, TimeUnit.SECONDS);
                    Instant expiresAt = null;
                    if (ttlSeconds != null && ttlSeconds > 0) {
                        expiresAt = Instant.now().plusSeconds(ttlSeconds);
                    }

                    return new AccountInfo(userId, jti, "Non info", expiresAt); // todo
                })
                .toList();
    }

    public String updateJti(JwtToken old_refreshToken) {
        String oldKey = buildKey(old_refreshToken);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(oldKey))) {
            String newJti = UserService.generateJti();
            redisTemplate.rename(oldKey, old_refreshToken.id() + "::" + newJti);
            return newJti;
        }
        throw new BadRequestException("Key is expire. Please log back into your account");
    }


    // todo для добавление девайса в redis
    public void logoutAll(Long id) {
        String pattern = id + "::*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void logoutOthers(JwtToken jwtToken) {
        String pattern = jwtToken.id() + "::*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            throw new RuntimeException("Inner server error. Poor synchronization with Redis");
        }

        keys.stream()
                .filter(k -> {
                    String[] parts = k.split("::", 2);
                    return parts.length == 2 && !parts[1].equals(jwtToken.jti());
                })
                .forEach(redisTemplate::delete);
    }

}
