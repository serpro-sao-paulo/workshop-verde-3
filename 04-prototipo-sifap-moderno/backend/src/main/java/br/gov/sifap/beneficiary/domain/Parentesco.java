package br.gov.sifap.beneficiary.domain;

/**
 * Parentesco do dependente (BR-033 / REQ-BEN-005).
 *
 * <p>Equivalência com CADDEPEND.NSN: FI=filho, CO=cônjuge, IR=irmão, OU=outro.
 */
public enum Parentesco {
    FILHO("FI"),
    CONJUGE("CO"),
    IRMAO("IR"),
    OUTRO("OU");

    private final String legacyCode;

    Parentesco(String legacyCode) {
        this.legacyCode = legacyCode;
    }

    public String legacyCode() {
        return legacyCode;
    }
}
