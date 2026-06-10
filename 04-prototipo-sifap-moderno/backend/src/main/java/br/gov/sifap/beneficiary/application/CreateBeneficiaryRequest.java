package br.gov.sifap.beneficiary.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * Requisição de cadastro de beneficiário (REQ-BEN-003).
 *
 * <p>Validação de fronteira com Bean Validation; regras de domínio (CPF, status,
 * nome composto) são aplicadas no service.
 */
public record CreateBeneficiaryRequest(
        @NotBlank(message = "CPF é obrigatório")
        String cpf,

        String nis,

        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotNull(message = "Data de nascimento é obrigatória")
        LocalDate birthDate,

        @NotBlank
        @Pattern(regexp = "[MF]", message = "Sexo deve ser 'M' ou 'F'")
        String sex
) {
}
