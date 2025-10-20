package Marisol_Mancera.fitpet.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

/**
 * JWT con clave simétrica (HS512) leyendo 'jwt.key' desde application.properties.
 * - 'jwt.key' es base64 (64 bytes) -> decodificamos y firmamos/verificamos con HS512.
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.key}")
    private String base64Secret;
    
     @Bean 
    public JwtEncoder jwtEncoder() {
        // decodificamos la clave Base64 generada (64 bytes = 512 bits)
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        return new NimbusJwtEncoder(new ImmutableSecret<>(keyBytes)); 
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