package br.gov.sifap.beneficiary.application;

import br.gov.sifap.beneficiary.domain.BeneficiaryRules;
import br.gov.sifap.beneficiary.domain.BeneficiaryStatus;
import br.gov.sifap.beneficiary.domain.Cpf;
import br.gov.sifap.beneficiary.infrastructure.BeneficiaryEntity;
import br.gov.sifap.beneficiary.infrastructure.BeneficiaryRepository;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de cadastro e consulta de beneficiários.
 *
 * <p>Implements REQ-BEN-001 (CPF módulo 11), REQ-BEN-002 (sem backdoor 000),
 * REQ-BEN-003 (campos obrigatórios), REQ-BEN-004 (status inicial / suspensão &gt;75),
 * REQ-BEN-006 (consulta por CPF/NIS com máscara).
 */
@Service
public class BeneficiaryService {

    private final BeneficiaryRepository repository;
    private final Clock clock;

    public BeneficiaryService(BeneficiaryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /**
     * Cadastra um beneficiário aplicando todas as regras de domínio.
     *
     * @throws br.gov.sifap.beneficiary.domain.InvalidCpfException se o CPF for inválido (REQ-BEN-001/002)
     * @throws BeneficiaryValidationException se nome inválido ou CPF duplicado (REQ-BEN-003 / BR-024)
     */
    @Transactional
    public BeneficiaryResponse register(CreateBeneficiaryRequest request) {
        // REQ-BEN-001 / REQ-BEN-002: validação de CPF (lança InvalidCpfException)
        Cpf cpf = Cpf.of(request.cpf());

        // REQ-BEN-003: nome composto (nome + sobrenome)
        if (!BeneficiaryRules.isValidName(request.name())) {
            throw new BeneficiaryValidationException("Nome deve conter nome e sobrenome");
        }

        // BR-024: não permitir CPF duplicado
        if (repository.existsByCpf(cpf.value())) {
            throw new BeneficiaryValidationException("CPF já cadastrado");
        }

        LocalDate today = LocalDate.now(clock);

        // REQ-BEN-004: status inicial ACTIVE; idade > 75 → SUSPENDED
        BeneficiaryStatus status = BeneficiaryRules.initialStatus(request.birthDate(), today);

        BeneficiaryEntity entity = new BeneficiaryEntity(
                cpf.value(),
                request.nis(),
                request.name().trim(),
                request.birthDate(),
                request.sex(),
                status,
                today
        );

        BeneficiaryEntity saved = repository.save(entity);
        return BeneficiaryResponse.from(saved, cpf.masked());
    }

    /** Consulta por CPF (REQ-BEN-006). */
    @Transactional(readOnly = true)
    public BeneficiaryResponse findByCpf(String rawCpf) {
        Cpf cpf = Cpf.of(rawCpf);
        BeneficiaryEntity entity = repository.findByCpf(cpf.value())
                .orElseThrow(() -> new BeneficiaryNotFoundException(cpf.masked()));
        return BeneficiaryResponse.from(entity, cpf.masked());
    }

    /** Consulta por NIS (REQ-BEN-006 / BR-047). */
    @Transactional(readOnly = true)
    public BeneficiaryResponse findByNis(String nis) {
        BeneficiaryEntity entity = repository.findByNis(nis)
                .orElseThrow(() -> new BeneficiaryNotFoundException("NIS " + nis));
        return BeneficiaryResponse.from(entity, Cpf.of(entity.getCpf()).masked());
    }
}
