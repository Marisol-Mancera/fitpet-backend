package Marisol_Mancera.fitpet.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import java.util.Base64;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

/**
 * JWT con clave simétrica (HS512) leyendo 'jwt.key' desde application.properties.
 * - 'jwt.key' es base64 (64 bytes) -> decodificamos y firmamos/verificamos con HS512.
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.key}")
    private String base64Secret;

    //  @Bean
    // public JwtEncoder jwtEncoder() {
    //     byte[] keyBytes = Base64.getDecoder().decode(base64Secret); 
    //     var secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
    //     return NimbusJwtEncoder.withSecretKey(secretKey).build();
    // }

    // @Bean //version que me da copilot
    // public JwtEncoder jwtEncoder() {
    //     byte[] keyBytes = Base64.getDecoder().decode(base64Secret); 
    //     var jwk = new OctetSequenceKey.Builder(keyBytes)
    //             .keyUse(KeyUse.SIGNATURE)
    //             .algorithm(JWSAlgorithm.HS512)
    //             .keyID(UUID.randomUUID().toString())
    //             .build();
    //     var jwkSet = new com.nimbusds.jose.jwk.JWKSet(jwk);
    //     var jwkSource = new ImmutableJWKSet<SecurityContext>(jwkSet);
    //     return new NimbusJwtEncoder(jwkSource);
    // }

     @Bean //version que me da gpt
    public JwtEncoder jwtEncoder() {
        // 🔐 Decodificamos la clave Base64 generada (64 bytes = 512 bits)
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        return new NimbusJwtEncoder(new ImmutableSecret<>(keyBytes)); // ✅ Compatibilidad total
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        var secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }
}