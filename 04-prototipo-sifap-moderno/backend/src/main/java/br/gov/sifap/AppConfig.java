package br.gov.sifap;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Beans de infraestrutura compartilhados. */
@Configuration
public class AppConfig {

    /** Clock injetável — permite testar regras dependentes de data (REQ-BEN-004). */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
