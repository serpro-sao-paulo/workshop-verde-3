package br.gov.sifap.beneficiary.infrastructure;

import br.gov.sifap.beneficiary.application.BeneficiaryResponse;
import br.gov.sifap.beneficiary.application.BeneficiaryService;
import br.gov.sifap.beneficiary.application.CreateBeneficiaryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints REST de beneficiário (REQ-BEN-003 / REQ-BEN-006). */
@RestController
@RequestMapping("/api/v1/beneficiaries")
@Tag(name = "Beneficiary", description = "Cadastro e consulta de beneficiários")
public class BeneficiaryController {

    private final BeneficiaryService service;

    public BeneficiaryController(BeneficiaryService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastra um beneficiário (REQ-BEN-001..004)")
    public ResponseEntity<BeneficiaryResponse> create(@Valid @RequestBody CreateBeneficiaryRequest request) {
        BeneficiaryResponse response = service.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Consulta beneficiário por CPF ou NIS (REQ-BEN-006)")
    public ResponseEntity<BeneficiaryResponse> find(
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String nis) {
        if (cpf != null && !cpf.isBlank()) {
            return ResponseEntity.ok(service.findByCpf(cpf));
        }
        if (nis != null && !nis.isBlank()) {
            return ResponseEntity.ok(service.findByNis(nis));
        }
        return ResponseEntity.badRequest().build();
    }
}
