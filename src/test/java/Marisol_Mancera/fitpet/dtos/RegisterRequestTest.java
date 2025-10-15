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

    @Test
    @DisplayName("Debe fallar si la contraseña tiene menos de 8 caracteres")
    void should_fail_when_password_too_short() {
        var dto = new RegisterRequest("pajaritopio@example.com", "A1!");

        var violations = validator.validate(dto);

        assertThat(violations, not(empty()));

        // (opcional) comprobamos que el mensaje contenga el texto esperado
        var messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        assertThat(messages, hasItem(containsString("al menos 8 caracteres")));
    }

    @Test
    @DisplayName("Debe fallar si la contraseña no contiene ningún dígito")
    void should_fail_when_password_without_digit() {
        // Arrange
        var dto = new RegisterRequest("pajaritopio@example.com", "Strong!Pass"); // sin dígitos

        // Act
        var violations = validator.validate(dto);

        // Assert
        assertThat(violations, not(empty()));
        var messages = violations.stream().map(ConstraintViolation::getMessage).toList();
        assertThat(messages, hasItem(containsString("al menos un número")));
    }

    @Test
    @DisplayName("Debe fallar si la contraseña no contiene ningún símbolo")
    void should_fail_when_password_without_symbol() {
        var dto = new RegisterRequest("pajaritopio@example.com", "Strong0Pass");

        var violations = validator.validate(dto);

        assertThat(violations, not(empty()));
        var messages = violations.stream().map(ConstraintViolation::getMessage).toList();
        assertThat(messages, hasItem(containsString("al menos un símbolo")));
    }


}
