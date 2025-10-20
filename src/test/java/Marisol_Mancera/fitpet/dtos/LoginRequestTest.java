package Marisol_Mancera.fitpet.dtos;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.*;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class LoginRequestTest {

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
    @DisplayName("Debe aceptar email válido y password no vacía")
    void should_accept_valid_email_and_non_blank_password() {
        var dto = new LoginRequest("owner@example.com", "Str0ng!Pass");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(dto);
        assertThat(violations, empty());
    }

    
}
