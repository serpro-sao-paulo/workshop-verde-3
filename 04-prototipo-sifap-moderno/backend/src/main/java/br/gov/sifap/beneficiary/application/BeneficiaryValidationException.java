package br.gov.sifap.beneficiary.application;

/** Lançada quando regras de cadastro são violadas (nome inválido, duplicidade, etc.). */
public class BeneficiaryValidationException extends RuntimeException {
    public BeneficiaryValidationException(String message) {
        super(message);
    }
}
