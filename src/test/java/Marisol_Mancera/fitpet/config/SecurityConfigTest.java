package Marisol_Mancera.fitpet.config;


import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    ApplicationContext context;

    @Autowired
    HttpSecurity http;

    @Test
    @DisplayName("Debe exponer SecurityFilterChain y AuthenticationManager como beans configurados")
    void should_expose_security_beans_correctly() throws Exception {
        var chain = context.getBean(SecurityFilterChain.class);
        var manager = context.getBean(AuthenticationManager.class);

        assertThat(chain).isNotNull();
        assertThat(manager).isNotNull();
    }
}
 
