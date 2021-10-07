package io.airbyte.oauth.flows;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import io.airbyte.commons.json.Jsons;
import io.airbyte.config.SourceOAuthParameter;
import io.airbyte.config.persistence.ConfigNotFoundException;
import io.airbyte.config.persistence.ConfigRepository;
import io.airbyte.oauth.OAuthFlowImplementation;
import io.airbyte.oauth.flows.zendesk.ZendeskOAuthFlow;
import io.airbyte.validation.json.JsonValidationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


public class ZendeskOAuthFlowIntegrationTest extends OAuthFlowIntegrationTest {
    protected static final Path CREDENTIALS_PATH = Path.of("secrets/zendesk_sunshine.json");

    @Override
    protected Path get_credentials_path() {
        return CREDENTIALS_PATH;
    }

    @Override
    protected OAuthFlowImplementation getFlowObject(ConfigRepository configRepository) {
        return new ZendeskOAuthFlow(configRepository);
    }



    @Test
    public void testFullGoogleOAuthFlow() throws InterruptedException, ConfigNotFoundException, IOException, JsonValidationException {
        int limit = 20;
        final UUID workspaceId = UUID.randomUUID();
        final UUID definitionId = UUID.randomUUID();
        final String fullConfigAsString = new String(Files.readAllBytes(CREDENTIALS_PATH));
        final JsonNode credentialsJson = Jsons.deserialize(fullConfigAsString);
        when(configRepository.listSourceOAuthParam()).thenReturn(List.of(new SourceOAuthParameter()
                .withOauthParameterId(UUID.randomUUID())
                .withSourceDefinitionId(definitionId)
                .withWorkspaceId(workspaceId)
                .withConfiguration(Jsons.jsonNode(ImmutableMap.builder()
                        .put("client_id", credentialsJson.get("client_id").asText())
                        .put("client_secret", credentialsJson.get("client_secret").asText())
                        .put("subdomain", credentialsJson.get("subdomain").asText())
                        .build()))));
        final String url = flow.getSourceConsentUrl(workspaceId, definitionId, REDIRECT_URL);
        LOGGER.info("Waiting for user consent at: {}", url);
        // TODO: To automate, start a selenium job to navigate to the Consent URL and click on allowing
        // access...
        while (!serverHandler.isSucceeded() && limit > 0) {
            Thread.sleep(1000);
            limit -= 1;
        }
        assertTrue(serverHandler.isSucceeded(), "Failed to get User consent on time");
        final Map<String, Object> params = flow.completeSourceOAuth(workspaceId, definitionId,
                Map.of("code", serverHandler.getParamValue()), REDIRECT_URL);
        LOGGER.info("Response from completing OAuth Flow is: {}", params.toString());
        assertTrue(params.containsKey("access_token"));
        assertTrue(params.get("access_token").toString().length() > 0);
    }
}
