package br.com.codenation.logstackapi.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@JsonPropertyOrder({"id", "name", "message", "email", "filters", "isArchive", "isActive", "createdBy"})
public class TriggerResponseDTO {

    @ApiModelProperty(value = "Identificador do gatilho", position = 1, example = "cbd9881e-88e9-4973-bfc0-5b4fcde29574")
    private UUID id;

    @ApiModelProperty(value = "Nome do gatilho", position = 2, example = "Level Erro em produção da API LogStack")
    private String name;

    @ApiModelProperty(value = "Mensagem para descrever o gatilho", position = 3, example = "Verificar com URGÊNCIA o serviço da aplicação")
    private String message;

    @ApiModelProperty(value = "Email para ser enviado o alerta do gatilho acionado", position = 4, example = "admin@example.com")
    private String email;

    @ApiModelProperty(value = "Filtros do gatilho", position = 5)
    private TriggerFilterResponseDTO filters;

    @ApiModelProperty(value = "Gatilho arquivado", position = 6)
    private Boolean isArchive;

    @ApiModelProperty(value = "Status do gatilho", position = 7)
    private Boolean isActive;

    @ApiModelProperty(value = "Usuário criador", position = 8)
    private UserResponseDTO createdBy;


}