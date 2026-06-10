package br.gov.sifap.beneficiary.domain;

/** Lançada quando um CPF é inválido (REQ-BEN-001 / REQ-BEN-002). */
public class InvalidCpfException extends RuntimeException {
    public InvalidCpfException(String raw) {
        super("CPF inválido: " + raw);
    }
}
