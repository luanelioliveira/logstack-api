package br.com.codenation.logstackapi.controller;

import br.com.codenation.logstackapi.builders.AlertBuilder;
import br.com.codenation.logstackapi.builders.UserBuilder;
import br.com.codenation.logstackapi.model.entity.Alert;
import br.com.codenation.logstackapi.model.entity.User;
import br.com.codenation.logstackapi.repository.AlertRepository;
import br.com.codenation.logstackapi.service.AlertService;
import br.com.codenation.logstackapi.service.SecurityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AlertControllerTest {

    private static String URI = "/api/v1/alerts";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AlertService alertService;

    @MockBean
    private AlertRepository alertRepository;

    @MockBean
    private SecurityService securityService;

    @Value("${security.oauth2.client.client-id}")
    private String client;

    @Value("${security.oauth2.client.client-secret}")
    private String secret;

    private ObjectMapper objectMapper = new ObjectMapper();
    private JacksonJsonParser parser = new JacksonJsonParser();
    private String token = "";

    @Before
    public void beforeTests() throws Exception {
        token = generateToken();
    }

    @Test
    public void dadoParametrosDaPagina_quandoPesquisarAlertas_entaoDevePaginaDeAlerta() throws Exception {

        User user = UserBuilder.codenation().build();

        Alert a1 = AlertBuilder.umAlert().build();
        Alert a2 = AlertBuilder.doisAlert().build();
        List<Alert> alerts = Arrays.asList(a1, a2);

        Mockito.when(securityService.getUserAuthenticated()).thenReturn(user);
        Mockito.when(alertRepository.findByTriggerCreatedById(user.getId())).thenReturn(alerts);

        ResultActions perform = mvc.perform(get(URI)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        perform.andExpect(jsonPath("$[0].trigger.message", is("Trigger 1")));
        perform.andExpect(jsonPath("$[1].log.title", is("Título")));

    }

    @Test
    public void dadoParametrosDaPagina_quandoPesquisarAlertasSemAutenticacao_entaoDeveRetornarErro() throws Exception {
        ResultActions perform = mvc.perform(get(URI)).andExpect(status().is(401));
    }

    @Test
    public void dadoAlertaNaoVisualizado_quandoConfirmarVisualizacao_deveRetornarAlertaAtualizado() throws Exception {

        Alert alert = AlertBuilder.umAlert().naoVisualizado().build();
        Mockito.when(alertRepository.save(alert)).thenReturn(alert);
        Mockito.when(alertRepository.findById(alert.getId())).thenReturn(Optional.of(alert));

        ResultActions perform = mvc.perform(post(URI + "/" + alert.getId() + "/view")
                .header("Authorization", token));

        perform.andExpect(status().isOk());
        perform.andExpect(jsonPath("$.isVisualized", is(Boolean.TRUE)));

    }

    @Test
    public void dadoAlertaNaoExistente_quandoConfirmacaoVisualizacao_deveRetornarAlertaNaoEncontrado() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(alertRepository.findById(id)).thenReturn(Optional.empty());

        ResultActions perform = mvc.perform(post(URI + "/" + id + "/view")
                .header("Authorization", token));

        perform.andExpect(status().isNotFound());
    }

    private String generateToken() throws Exception {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("username", "admin@admin.com");
        params.add("password", "admin");

        ResultActions login = mvc.perform(
                post("/oauth/token")
                        .params(params)
                        .accept("application/json;charset=UTF-8")
                        .with(httpBasic(client, secret)))
                .andExpect(status().isOk());

        String token = parser.parseMap(login
                .andReturn()
                .getResponse()
                .getContentAsString()).get("access_token").toString();

        return String.format("Bearer %s", token);
    }

}
