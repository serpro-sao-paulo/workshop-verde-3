package br.gov.sifap.beneficiary.application;

import br.gov.sifap.beneficiary.domain.BeneficiaryStatus;
import br.gov.sifap.beneficiary.infrastructure.BeneficiaryEntity;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resposta de beneficiário com CPF mascarado (REQ-BEN-006 / BR-049).
 */
public record BeneficiaryResponse(
        UUID id,
        String cpfMasked,
        String nis,
        String name,
        LocalDate birthDate,
        String sex,
        BeneficiaryStatus status
) {
    public static BeneficiaryResponse from(BeneficiaryEntity entity, String cpfMasked) {
        return new BeneficiaryResponse(
                entity.getId(),
                cpfMasked,
                entity.getNis(),
                entity.getName(),
                entity.getBirthDate(),
                entity.getSex(),
                entity.getStatus()
        );
    }
}
