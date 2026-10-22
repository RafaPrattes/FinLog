package com.ucb.finlog.security;

import com.ucb.finlog.model.Usuario;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthTokenService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    @Value("${app.auth.token-secret}")
    private String tokenSecret;

    @Value("${app.auth.token-expiration-seconds}")
    private long expirationSeconds;

    @PostConstruct
    void validarSegredo() {
        if (tokenSecret == null
                || tokenSecret.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SEGREDO
                || tokenSecret.contains("change-me")) {
            throw new IllegalStateException(
                    "APP_AUTH_TOKEN_SECRET deve ser definido com pelo menos "
                            + TAMANHO_MINIMO_SEGREDO + " caracteres aleatorios");
        }
    }

    public String gerarToken(Usuario usuario) {
        long expiresAt = Instant.now().plusSeconds(expirationSeconds).getEpochSecond();
        String payload = usuario.getId() + ":" + usuario.getEmail() + ":" + expiresAt;
        String encodedPayload = encode(payload);
        return encodedPayload + "." + sign(encodedPayload);
    }

    public UsuarioAutenticado validarToken(String token) {
        String[] payloadParts = lerPayload(token);
        long expiresAt = Long.parseLong(payloadParts[2]);
        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new IllegalArgumentException("Token expirado");
        }

        return new UsuarioAutenticado(Long.parseLong(payloadParts[0]), payloadParts[1]);
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private String[] lerPayload(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 2 || !assinaturaValida(parts[0], parts[1])) {
            throw new IllegalArgumentException("Token invalido");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String[] payloadParts = payload.split(":");
        if (payloadParts.length != 3) {
            throw new IllegalArgumentException("Token invalido");
        }
        return payloadParts;
    }

    private boolean assinaturaValida(String payload, String assinatura) {
        return MessageDigest.isEqual(
                sign(payload).getBytes(StandardCharsets.UTF_8),
                assinatura.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel assinar o token", exception);
        }
    }
}
