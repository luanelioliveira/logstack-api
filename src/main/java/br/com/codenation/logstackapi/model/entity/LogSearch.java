package br.com.codenation.logstackapi.model.entity;

import br.com.codenation.logstackapi.model.enums.LogEnvironment;
import br.com.codenation.logstackapi.model.enums.LogLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogSearch {

    private String title;
    private String appName;
    private String host;
    private String ip;
    private LogEnvironment environment;
    private String content;
    private LogLevel level;
    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
    private User user;

    public void validationValues() {
        if (endTimestamp.isBefore(startTimestamp)) {
            throw new IllegalArgumentException("Data de início deve anteceder a data final");
        }
    }

}
