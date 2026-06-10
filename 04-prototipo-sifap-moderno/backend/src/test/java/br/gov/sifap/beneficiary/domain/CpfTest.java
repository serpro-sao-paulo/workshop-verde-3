package br.gov.sifap.beneficiary.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes de equivalência do CPF contra VALBENEF.NSN.
 * Implements REQ-BEN-001 (módulo 11) e REQ-BEN-002 (sem backdoor 000 / MYS-007).
 */
class CpfTest {

    @Test
    @DisplayName("REQ-BEN-001: CPF válido passa no módulo 11")
    void validCpfPasses() {
        assertThat(Cpf.isValid("111.444.777-35")).isTrue();
        assertThat(Cpf.isValid("52998224725")).isTrue();
    }

    @Test
    @DisplayName("REQ-BEN-001: CPF com dígito verificador errado é rejeitado")
    void invalidCheckDigitFails() {
        assertThat(Cpf.isValid("111.444.777-00")).isFalse();
        assertThat(Cpf.isValid("12345678900")).isFalse();
    }

    @Test
    @DisplayName("REQ-BEN-002: CPF 000.000.000-00 é rejeitado (corrige backdoor MYS-007)")
    void allZerosRejected() {
        assertThat(Cpf.isValid("000.000.000-00")).isFalse();
    }

    @Test
    @DisplayName("REQ-BEN-001: CPF com todos os dígitos iguais é rejeitado")
    void allEqualDigitsRejected() {
        assertThat(Cpf.isValid("111.111.111-11")).isFalse();
        assertThat(Cpf.isValid("99999999999")).isFalse();
    }

    @Test
    @DisplayName("CPF com tamanho inválido é rejeitado")
    void wrongLengthRejected() {
        assertThat(Cpf.isValid("123")).isFalse();
        assertThat(Cpf.isValid("")).isFalse();
        assertThat(Cpf.isValid(null)).isFalse();
    }

    @Test
    @DisplayName("Cpf.of lança exceção para CPF inválido")
    void factoryThrowsOnInvalid() {
        assertThatThrownBy(() -> Cpf.of("000.000.000-00"))
                .isInstanceOf(InvalidCpfException.class);
    }

    @Test
    @DisplayName("REQ-BEN-006: máscara oculta os 6 primeiros dígitos")
    void maskHidesFirstSixDigits() {
        Cpf cpf = Cpf.of("111.444.777-35");
        assertThat(cpf.masked()).isEqualTo("***.***.777-35");
    }

    @Test
    @DisplayName("Cpf.value retorna apenas dígitos")
    void valueReturnsDigitsOnly() {
        assertThat(Cpf.of("111.444.777-35").value()).isEqualTo("11144477735");
    }
}
