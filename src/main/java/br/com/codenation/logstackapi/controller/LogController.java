package br.com.codenation.logstackapi.controller;

import br.com.codenation.logstackapi.dto.request.LogRequestDTO;
import br.com.codenation.logstackapi.dto.response.LogDetailResponseDTO;
import br.com.codenation.logstackapi.dto.response.LogResponseDTO;
import br.com.codenation.logstackapi.exception.ApiError;
import br.com.codenation.logstackapi.mappers.LogMapper;
import br.com.codenation.logstackapi.model.entity.LogSearch;
import br.com.codenation.logstackapi.model.entity.User;
import br.com.codenation.logstackapi.model.enums.LogEnvironment;
import br.com.codenation.logstackapi.model.enums.LogLevel;
import br.com.codenation.logstackapi.service.LogService;
import br.com.codenation.logstackapi.service.SecurityService;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping(value = "/api/v1")
@Api(tags = {"Logs"}, description = "Endpoint para gerenciamento dos logs")
public class LogController {

    private SecurityService securityService;
    private LogService logService;
    private LogMapper mapper;

    @ApiOperation(
            value = "Exporta os logs pesquisados por um filtro",
            notes = "Método utilizado para exportar os logs pesquisados por um filtro."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Requisição mal formatada", response = ApiError.class),
            @ApiResponse(code = 500, message = "Erro na api", response = ApiError.class)
    })
    @GetMapping(value = "/logs/export", produces = "text/csv")
    private void exportCSV(
            HttpServletResponse response,
            @RequestParam(value = "title", required = false) Optional<String> title,
            @RequestParam(value = "appName", required = false) Optional<String> appName,
            @RequestParam(value = "host", required = false) Optional<String> host,
            @RequestParam(value = "ip", required = false) Optional<String> ip,
            @RequestParam(value = "environment", required = false) Optional<LogEnvironment> environment,
            @RequestParam(value = "content", required = false) Optional<String> content,
            @RequestParam(value = "level", required = false) Optional<LogLevel> level,
            @RequestParam(value = "startTimestamp", required = false, defaultValue = "2019-09-01")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTimestamp,
            @RequestParam(value = "endTimestamp", required = false, defaultValue = "2019-09-30")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTimestamp,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {

        Sort sort = Sort.by(Sort.Direction.DESC, "detail.timestamp");

        User user = securityService.getUserAuthenticated();
        LogSearch search = LogSearch.builder()
                .title(title.map(String::toLowerCase).orElse(null))
                .appName(appName.map(String::toLowerCase).orElse(null))
                .host(host.map(String::toLowerCase).orElse(null))
                .ip(ip.map(String::toLowerCase).orElse(null))
                .environment(environment.orElse(null))
                .content(content.orElse(null))
                .level(level.orElse(null))
                .user(user)
                .startTimestamp(LocalDateTime.of(startTimestamp, LocalTime.of(0, 0, 0)))
                .endTimestamp(LocalDateTime.of(endTimestamp, LocalTime.of(23, 59, 59)))
                .build();

        List<LogResponseDTO> logs = logService.find(search, page, size, sort).map(mapper::map).getContent();

        String filename = "logs.csv";

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"");

        StatefulBeanToCsv<LogResponseDTO> writer = new StatefulBeanToCsvBuilder<LogResponseDTO>(response.getWriter())
                .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .withOrderedResults(false)
                .build();

        writer.write(logs);

    }

    @ApiOperation(
            value = "Recupera todos os logs cadastrados",
            notes = "Método utilizado para recuperar todos os logs cadastrados."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LogResponseDTO.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Requisição mal formatada", response = ApiError.class),
            @ApiResponse(code = 500, message = "Erro na api", response = ApiError.class)
    })
    @GetMapping(value = "/logs", produces = MediaType.APPLICATION_JSON_VALUE)
    private Page<LogResponseDTO> find(
            @RequestParam(value = "title", required = false) Optional<String> title,
            @RequestParam(value = "appName", required = false) Optional<String> appName,
            @RequestParam(value = "host", required = false) Optional<String> host,
            @RequestParam(value = "ip", required = false) Optional<String> ip,
            @RequestParam(value = "content", required = false) Optional<String> content,
            @RequestParam(value = "environment", required = false) Optional<LogEnvironment> environment,
            @RequestParam(value = "level", required = false) Optional<LogLevel> level,
            @RequestParam(value = "startTimestamp", required = false, defaultValue = "2019-09-01")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTimestamp,
            @RequestParam(value = "endTimestamp", required = false, defaultValue = "2019-09-30")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTimestamp,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {

        Sort sort = Sort.by(Sort.Direction.DESC, "detail.timestamp");

        User user = securityService.getUserAuthenticated();
        LogSearch search = LogSearch.builder()
                .title(title.map(String::toLowerCase).orElse(null))
                .appName(appName.map(String::toLowerCase).orElse(null))
                .host(host.map(String::toLowerCase).orElse(null))
                .ip(ip.map(String::toLowerCase).orElse(null))
                .environment(environment.orElse(null))
                .content(content.orElse(null))
                .level(level.orElse(null))
                .user(user)
                .startTimestamp(LocalDateTime.of(startTimestamp, LocalTime.of(0, 0, 0)))
                .endTimestamp(LocalDateTime.of(endTimestamp, LocalTime.of(23, 59, 59)))
                .build();

        return logService.find(search, page, size, sort).map(mapper::map);
    }


    @ApiOperation(
            value = "Recupera um log específico.",
            notes = "Método utilizado para recuperar um log específico."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LogDetailResponseDTO.class),
            @ApiResponse(code = 400, message = "Requisição mal formatada", response = ApiError.class),
            @ApiResponse(code = 404, message = "Log não encontrado", response = ApiError.class),
            @ApiResponse(code = 500, message = "Erro na api", response = ApiError.class)
    })
    @GetMapping(value = "/logs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    private LogResponseDTO getById(@PathVariable UUID id) {
        return mapper.map(logService.findById(id));
    }

    @ApiOperation(
            value = "Arquiva um log específico.",
            notes = "Método utilizado para arquivar um log específico."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Log arquivado", response = LogResponseDTO.class),
            @ApiResponse(code = 400, message = "Requisição mal formatada", response = ApiError.class),
            @ApiResponse(code = 404, message = "Log não encontrado", response = ApiError.class),
            @ApiResponse(code = 500, message = "Erro na api", response = ApiError.class)
    })
    @PostMapping(value = "/logs/{id}/archive", produces = MediaType.APPLICATION_JSON_VALUE)
    private LogResponseDTO archive(@PathVariable UUID id) {
        return mapper.map(logService.archive(id));
    }

    @ApiOperation(
            value = "Desarquiva um log específico.",
            notes = "Método utilizado para desarquivar um log específico."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Log desarquivado", response = LogResponseDTO.class),
            @ApiResponse(code = 400, message = "Requisição mal formatada", response = ApiError.class),
            @ApiResponse(code = 404, message = "Log não encontrado", response = ApiError.class),
            @ApiResponse(code = 500, message = "Erro na api", response = ApiError.class)
    })
    @DeleteMapping(value = "/logs/{id}/archive", produces = MediaType.APPLICATION_JSON_VALUE)
    private LogResponseDTO unarchive(@PathVariable UUID id) {
        return mapper.map(logService.unarchive(id));
    }

    @ApiOperation(
            value = "Cria um log",
            notes = "Método utilizado para criar um log"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Log criado", response = LogResponseDTO.class),
            @ApiResponse(code = 400, message = "Requisão mal formatada", response = ApiError.class),
            @ApiResponse(code = 500, message = "Erro na apo", response = ApiError.class)
    })
    @PostMapping(value = "/logs", produces = MediaType.APPLICATION_JSON_VALUE)
    private LogResponseDTO save(@RequestParam(value = "apiKey") UUID apiKey,
                                @Valid @RequestBody LogRequestDTO dto) {
        return mapper.map(logService.add(apiKey, dto));
    }
}
