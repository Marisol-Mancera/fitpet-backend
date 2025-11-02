package Marisol_Mancera.fitpet.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import Marisol_Mancera.fitpet.user.UserEntity;
import Marisol_Mancera.fitpet.user.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

/**
 * Servicio de autenticación y emisión de JWT.
 * - Verifica credenciales (username/password) contra la BD.
 * - Construye claims estándar + 'scope' en base a los roles del usuario.
 * - Firma el token usando el JwtEncoder configurado (HS512).
 * 
 * Modificado: Tiempo de expiración aumentado a 2 horas para desarrollo.
 */
@Service
public class JwtTokenService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public JwtTokenService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    /**
     * Autentica usuario y genera un JWT.
     * @throws BadCredentialsException
     */
    public String loginAndGenerateToken(String username, String rawPassword) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Mapeamos ROLE_X -> X para scope (espacio separado)
        String scope = user.getRoles() == null ? "" :
                user.getRoles().stream()
                        .map(r -> r.getName())
                        .map(roleName -> roleName.startsWith("ROLE_") ? roleName.substring(5) : roleName)
                        .collect(Collectors.joining(" "));

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(2, ChronoUnit.HOURS)) // TTL: 2 horas (desarrollo)
                .claim("scope", scope)
                .build();

        // Firmar con HS512 (header lo gestionará el encoder/decoder configurado)
        JwsHeader jws = JwsHeader.with(MacAlgorithm.HS512).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jws, claims)).getTokenValue();
    }
}