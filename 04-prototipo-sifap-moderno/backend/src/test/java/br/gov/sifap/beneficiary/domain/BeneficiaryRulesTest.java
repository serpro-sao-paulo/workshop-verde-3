package br.gov.sifap.beneficiary.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes de equivalência das regras de cadastro contra CADBENEF.NSN / CADDEPEND.NSN.
 * Implements REQ-BEN-003, REQ-BEN-004, REQ-BEN-005.
 */
class BeneficiaryRulesTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 10);

    @Test
    @DisplayName("REQ-BEN-004: beneficiário com 40 anos recebe status ACTIVE")
    void youngBeneficiaryIsActive() {
        LocalDate birth = TODAY.minusYears(40);
        assertThat(BeneficiaryRules.initialStatus(birth, TODAY)).isEqualTo(BeneficiaryStatus.ACTIVE);
    }

    @Test
    @DisplayName("REQ-BEN-004: beneficiário com 80 anos é SUSPENDED (BR-027)")
    void elderlyBeneficiaryIsSuspended() {
        LocalDate birth = TODAY.minusYears(80);
        assertThat(BeneficiaryRules.initialStatus(birth, TODAY)).isEqualTo(BeneficiaryStatus.SUSPENDED);
    }

    @Test
    @DisplayName("REQ-BEN-004: exatamente 75 anos ainda é ACTIVE (limite > 75)")
    void exactlySeventyFiveIsActive() {
        LocalDate birth = TODAY.minusYears(75);
        assertThat(BeneficiaryRules.initialStatus(birth, TODAY)).isEqualTo(BeneficiaryStatus.ACTIVE);
    }

    @Test
    @DisplayName("REQ-BEN-004: 76 anos é SUSPENDED")
    void seventySixIsSuspended() {
        LocalDate birth = TODAY.minusYears(76);
        assertThat(BeneficiaryRules.initialStatus(birth, TODAY)).isEqualTo(BeneficiaryStatus.SUSPENDED);
    }

    @Test
    @DisplayName("REQ-BEN-003: nome com nome e sobrenome é válido")
    void compoundNameIsValid() {
        assertThat(BeneficiaryRules.isValidName("Maria Silva")).isTrue();
    }

    @Test
    @DisplayName("REQ-BEN-003: nome sem sobrenome é inválido")
    void singleNameIsInvalid() {
        assertThat(BeneficiaryRules.isValidName("Maria")).isFalse();
        assertThat(BeneficiaryRules.isValidName("  Maria  ")).isFalse();
    }

    @Test
    @DisplayName("REQ-BEN-003: nome em branco ou nulo é inválido")
    void blankNameIsInvalid() {
        assertThat(BeneficiaryRules.isValidName("")).isFalse();
        assertThat(BeneficiaryRules.isValidName("   ")).isFalse();
        assertThat(BeneficiaryRules.isValidName(null)).isFalse();
    }

    @Test
    @DisplayName("REQ-BEN-005: permite incluir dependente até o 5º")
    void allowsUpToFiveDependents() {
        assertThat(BeneficiaryRules.canAddDependent(0)).isTrue();
        assertThat(BeneficiaryRules.canAddDependent(4)).isTrue();
    }

    @Test
    @DisplayName("REQ-BEN-005: bloqueia o 6º dependente (BR-032 / MYS-002)")
    void blocksSixthDependent() {
        assertThat(BeneficiaryRules.canAddDependent(5)).isFalse();
    }
}
