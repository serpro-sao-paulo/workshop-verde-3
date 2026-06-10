package br.gov.sifap.beneficiary.domain;

/**
 * CPF value object.
 *
 * <p>Equivalência com VALBENEF.NSN#L178-L215 (algoritmo módulo 11 da Receita Federal).
 * Implements REQ-BEN-001 (validação módulo 11) e REQ-BEN-002 (CPFs iniciados com 000
 * NÃO devem burlar a validação — corrige o backdoor MYS-007 do legado).
 */
public final class Cpf {

    private final String digits;

    private Cpf(String digits) {
        this.digits = digits;
    }

    /**
     * Cria um CPF validado. Lança {@link InvalidCpfException} se inválido.
     */
    public static Cpf of(String raw) {
        String normalized = normalize(raw);
        if (!isValid(normalized)) {
            throw new InvalidCpfException(raw);
        }
        return new Cpf(normalized);
    }

    /**
     * Valida um CPF pelo algoritmo módulo 11.
     *
     * <p>REQ-BEN-001: dígitos verificadores conferidos.
     * REQ-BEN-002: CPFs com todos os dígitos iguais (inclusive 000.000.000-00)
     * são rejeitados — o legado abria exceção para os iniciados com 000.
     */
    public static boolean isValid(String raw) {
        String cpf = normalize(raw);
        if (cpf.length() != 11) {
            return false;
        }
        if (allDigitsEqual(cpf)) {
            return false; // rejeita 000.000.000-00 e demais sequências iguais
        }
        int dv1 = checkDigit(cpf, 9, 10);
        int dv2 = checkDigit(cpf, 10, 11);
        return dv1 == (cpf.charAt(9) - '0') && dv2 == (cpf.charAt(10) - '0');
    }

    private static int checkDigit(String cpf, int length, int startWeight) {
        int sum = 0;
        int weight = startWeight;
        for (int i = 0; i < length; i++) {
            sum += (cpf.charAt(i) - '0') * weight;
            weight--;
        }
        int rest = sum % 11;
        return rest < 2 ? 0 : 11 - rest;
    }

    private static boolean allDigitsEqual(String cpf) {
        char first = cpf.charAt(0);
        for (int i = 1; i < cpf.length(); i++) {
            if (cpf.charAt(i) != first) {
                return false;
            }
        }
        return true;
    }

    private static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("\\D", "");
    }

    /** Apenas dígitos (11 caracteres). */
    public String value() {
        return digits;
    }

    /**
     * Máscara para exibição: {@code ***.***.NNN-NN}.
     *
     * <p>Equivalência com CONSBENF.NSN (BR-049 / REQ-BEN-006) — oculta os 6 primeiros
     * dígitos, mantendo os 3 do meio e os 2 verificadores. Corrige o bug de máscara
     * documentado no legado.
     */
    public String masked() {
        return "***.***." + digits.substring(6, 9) + "-" + digits.substring(9, 11);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cpf other)) {
            return false;
        }
        return digits.equals(other.digits);
    }

    @Override
    public int hashCode() {
        return digits.hashCode();
    }

    @Override
    public String toString() {
        return masked();
    }
}
