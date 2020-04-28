package br.com.codenation.logstackapi.config.jpa;


import br.com.codenation.logstackapi.audit.AuditorAwareImpl;
import br.com.codenation.logstackapi.model.entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {

    @Bean
    public AuditorAware<User> auditorAware() {
        return new AuditorAwareImpl();
    }

}