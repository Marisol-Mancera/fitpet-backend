package Marisol_Mancera.fitpet.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Validación de RegisterRequest")
class RegisterRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    @DisplayName("Debe aceptar email válido y contraseña fuerte")
    void should_accept_valid_email_and_strong_password() {
        var dto = new RegisterRequest("pajaritopio@example.com", "Str0ng!Pass");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(dto);
        assertThat(violations, empty());
}
}
