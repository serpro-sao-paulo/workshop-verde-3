package br.gov.sifap.beneficiary.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositório do beneficiário (Spring Data JPA). */
public interface BeneficiaryRepository extends JpaRepository<BeneficiaryEntity, UUID> {

    boolean existsByCpf(String cpf);

    Optional<BeneficiaryEntity> findByCpf(String cpf);

    Optional<BeneficiaryEntity> findByNis(String nis);
}
