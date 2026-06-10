package br.gov.sifap.beneficiary.application;

/** Lançada quando um beneficiário não é encontrado na consulta (REQ-BEN-006). */
public class BeneficiaryNotFoundException extends RuntimeException {
    public BeneficiaryNotFoundException(String key) {
        super("Beneficiário não encontrado: " + key);
    }
}
