package br.gov.sifap.beneficiary.domain;

/**
 * Status do beneficiário (BR-048 / REQ-BEN-004).
 *
 * <p>Equivalência com o legado: A=ACTIVE, S=SUSPENDED, C=CANCELLED, I=INACTIVE, D=DISCHARGED.
 */
public enum BeneficiaryStatus {
    ACTIVE("A"),
    SUSPENDED("S"),
    CANCELLED("C"),
    INACTIVE("I"),
    DISCHARGED("D");

    private final String legacyCode;

    BeneficiaryStatus(String legacyCode) {
        this.legacyCode = legacyCode;
    }

    public String legacyCode() {
        return legacyCode;
    }

    public static BeneficiaryStatus fromLegacyCode(String code) {
        for (BeneficiaryStatus s : values()) {
            if (s.legacyCode.equals(code)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Status legado desconhecido: " + code);
    }
}
