package br.gov.sifap.beneficiary.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.sifap.beneficiary.domain.BeneficiaryStatus;
import br.gov.sifap.beneficiary.domain.InvalidCpfException;
import br.gov.sifap.beneficiary.infrastructure.BeneficiaryEntity;
import br.gov.sifap.beneficiary.infrastructure.BeneficiaryRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Testes do BeneficiaryService com repositório mockado (sem banco).
 * Implements REQ-BEN-001..004 + BR-024 (duplicidade).
 */
class BeneficiaryServiceTest {

    private BeneficiaryRepository repository;
    private BeneficiaryService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(BeneficiaryRepository.class);
        Clock fixed = Clock.fixed(
                LocalDate.of(2026, 6, 10).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault());
        service = new BeneficiaryService(repository, fixed);
        when(repository.save(any(BeneficiaryEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("REQ-BEN-004: cadastro de adulto resulta em status ACTIVE")
    void registersActiveAdult() {
        when(repository.existsByCpf("11144477735")).thenReturn(false);

        var request = new CreateBeneficiaryRequest(
                "111.444.777-35", "12345678901", "Maria Silva",
                LocalDate.of(1986, 1, 1), "F");

        BeneficiaryResponse response = service.register(request);

        assertThat(response.status()).isEqualTo(BeneficiaryStatus.ACTIVE);
        assertThat(response.cpfMasked()).isEqualTo("***.***.777-35");
    }

    @Test
    @DisplayName("REQ-BEN-004: cadastro de idoso (>75) resulta em SUSPENDED")
    void registersSuspendedElderly() {
        when(repository.existsByCpf("11144477735")).thenReturn(false);

        var request = new CreateBeneficiaryRequest(
                "111.444.777-35", null, "Jose Souza",
                LocalDate.of(1940, 1, 1), "M");

        BeneficiaryResponse response = service.register(request);

        assertThat(response.status()).isEqualTo(BeneficiaryStatus.SUSPENDED);
    }

    @Test
    @DisplayName("REQ-BEN-002: CPF 000 é rejeitado e nada é salvo")
    void rejectsZeroCpf() {
        var request = new CreateBeneficiaryRequest(
                "000.000.000-00", null, "Maria Silva",
                LocalDate.of(1986, 1, 1), "F");

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(InvalidCpfException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("REQ-BEN-003: nome sem sobrenome é rejeitado")
    void rejectsSingleName() {
        when(repository.existsByCpf("11144477735")).thenReturn(false);

        var request = new CreateBeneficiaryRequest(
                "111.444.777-35", null, "Maria",
                LocalDate.of(1986, 1, 1), "F");

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BeneficiaryValidationException.class)
                .hasMessageContaining("sobrenome");
    }

    @Test
    @DisplayName("BR-024: CPF duplicado é rejeitado")
    void rejectsDuplicateCpf() {
        when(repository.existsByCpf("11144477735")).thenReturn(true);

        var request = new CreateBeneficiaryRequest(
                "111.444.777-35", null, "Maria Silva",
                LocalDate.of(1986, 1, 1), "F");

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BeneficiaryValidationException.class)
                .hasMessageContaining("já cadastrado");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("REQ-BEN-004: persiste data de cadastro e atualização")
    void persistsAuditDates() {
        when(repository.existsByCpf("11144477735")).thenReturn(false);

        var request = new CreateBeneficiaryRequest(
                "111.444.777-35", null, "Maria Silva",
                LocalDate.of(1986, 1, 1), "F");

        service.register(request);

        ArgumentCaptor<BeneficiaryEntity> captor = ArgumentCaptor.forClass(BeneficiaryEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(captor.getValue().getUpdatedAt()).isEqualTo(LocalDate.of(2026, 6, 10));
    }
}
