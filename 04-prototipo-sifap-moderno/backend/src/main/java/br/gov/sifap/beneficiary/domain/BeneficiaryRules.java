package br.gov.sifap.beneficiary.domain;

import java.time.LocalDate;
import java.time.Period;

/**
 * Regras de negócio puras do cadastro de beneficiário, extraídas do legado.
 *
 * <p>Sem dependência de Spring/JPA — testáveis isoladamente (testes de equivalência).
 */
public final class BeneficiaryRules {

    /** Limite de dependentes por beneficiário (CADDEPEND.NSN#L63 / BR-032 / REQ-BEN-005). */
    public static final int MAX_DEPENDENTS = 5;

    /** Idade acima da qual o beneficiário é suspenso (CADBENEF.NSN#L167-L168 / BR-027). */
    public static final int SUSPENSION_AGE = 75;

    private BeneficiaryRules() {
    }

    /**
     * Status inicial no cadastro (REQ-BEN-004 / BR-026, BR-027).
     *
     * <p>Padrão ACTIVE; idade &gt; 75 anos é automaticamente SUSPENDED.
     */
    public static BeneficiaryStatus initialStatus(LocalDate birthDate, LocalDate referenceDate) {
        if (ageInYears(birthDate, referenceDate) > SUSPENSION_AGE) {
            return BeneficiaryStatus.SUSPENDED;
        }
        return BeneficiaryStatus.ACTIVE;
    }

    public static int ageInYears(LocalDate birthDate, LocalDate referenceDate) {
        return Period.between(birthDate, referenceDate).getYears();
    }

    /**
     * Validação de nome (REQ-BEN-003 / BR-021, BR-051): exige nome + sobrenome
     * (ao menos um espaço entre tokens não vazios).
     */
    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        String[] parts = trimmed.split("\\s+");
        return parts.length >= 2 && !parts[0].isEmpty() && !parts[1].isEmpty();
    }

    /**
     * Limite de dependentes (REQ-BEN-005 / BR-032): não permite o 6º dependente.
     *
     * @param currentCount quantidade atual de dependentes
     * @return true se ainda é possível incluir mais um
     */
    public static boolean canAddDependent(int currentCount) {
        return currentCount < MAX_DEPENDENTS;
    }
}
