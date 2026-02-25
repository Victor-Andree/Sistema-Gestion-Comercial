package com.gestion.sgc.infraestructure.security.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    @Value("${jwt.expiration:86400000}") // 24 horas por defecto
    private long tokenExpirationMs;

    public void addToBlacklist(String token) {
        blacklist.put(token, System.currentTimeMillis());
        log.info("Token agregado a blacklist. Tamaño actual: {}", blacklist.size());

        if (blacklist.size() > 10000) {
            cleanupExpiredTokens();
        }
    }

    public boolean isTokenBlacklisted(String token) {
        Long addedTime = blacklist.get(token);
        if (addedTime == null) {
            return false;
        }

        if (System.currentTimeMillis() - addedTime > tokenExpirationMs) {
            blacklist.remove(token);
            return false;
        }

        return true;
    }

    @Scheduled(fixedDelay = 3600000) // Cada hora
    public void cleanupExpiredTokens() {
        int beforeSize = blacklist.size();
        long now = System.currentTimeMillis();

        blacklist.entrySet().removeIf(entry ->
                now - entry.getValue() > tokenExpirationMs
        );

        int afterSize = blacklist.size();
        log.info("Limpieza automática: {} tokens eliminados, {} tokens activos en blacklist",
                beforeSize - afterSize, afterSize);
    }

}
