package Marisol_Mancera.fitpet.dtos;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

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

    @Test
    @DisplayName("Debe fallar si el email no es válido")
    void should_fail_when_email_is_invalid() {

        var dto = new RegisterRequest("not-an-email", "Str0ng!Pass");

        var violations = validator.validate(dto);

        assertThat(violations, not(empty()));
        var messages = violations.stream().map(ConstraintViolation::getMessage).toList();
        assertThat(messages, hasItem(containsString("correo electrónico no es válido")));
    }

    @Test
    @DisplayName("Debe rechazar contraseña con menos de 8 caracteres")
    void should_reject_password_shorter_than_8_characters() {
        var dto = new RegisterRequest("pajaritopio@example.com", "Abc!12");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(dto);

        assertThat(violations, is(not(empty())));
        boolean hasPasswordViolation = violations.stream()
                .anyMatch(v -> "password".equals(v.getPropertyPath().toString()));
        assertThat(hasPasswordViolation, is(true));
    }

    @Test
    @DisplayName("Debe rechazar contraseña sin ningún dígito")
    void should_reject_password_without_any_digit() {
        var dto = new RegisterRequest("pajaritopio@example.com", "Abc!defg");
        var violations = validator.validate(dto);

        // Debe haber al menos una violación y ser sobre el campo "password"
        assertThat(violations, is(not(empty())));
        boolean hasPasswordViolation = violations.stream()
                .anyMatch(v -> "password".equals(v.getPropertyPath().toString()));
        assertThat(hasPasswordViolation, is(true));
    }

    @Test
    @DisplayName("Debe rechazar contraseña sin ningún simbolo")
    void should_reject_password_without_any_symbol() {
        var dto = new RegisterRequest("pajaritopio@example.com", "Abc1defg");
        var violations = validator.validate(dto);

        assertThat(violations, is(not(empty())));
        boolean hasPasswordViolation = violations.stream()
                .anyMatch(v -> "password".equals(v.getPropertyPath().toString()));
        assertThat(hasPasswordViolation, is(true));
    }

}
