package jmri.server.json.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
import jmri.server.json.JsonSocketService;
import jmri.web.server.WebServerPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood
 */
public class JsonUtilSocketService extends JsonSocketService<JsonUtilHttpService> {

    private PropertyChangeListener rrNameListener;
    private static final Logger log = LoggerFactory.getLogger(JsonUtilSocketService.class);

    public JsonUtilSocketService(JsonConnection connection) {
        super(connection, new JsonUtilHttpService(connection.getObjectMapper()));
    }

    /**
     * Package protected method for unit testing that allows a test HTTP service
     * to be used.
     * 
     * @param connection the connection to use
     * @param service    the supporting HTTP service
     */
    JsonUtilSocketService(JsonConnection connection, JsonUtilHttpService service) {
        super(connection, service);
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        String name = data.path(JSON.NAME).asText();
        switch (type) {
            case JSON.LOCALE:
                // do nothing - we only want to prevent an error at this point
                break;
            case JSON.PING:
                this.connection.sendMessage(this.connection.getObjectMapper().createObjectNode().put(JSON.TYPE, JSON.PONG), request.id);
                break;
            case JSON.GOODBYE:
                this.connection.sendMessage(this.connection.getObjectMapper().createObjectNode().put(JSON.TYPE, JSON.GOODBYE), request.id);
                break;
            case JSON.RAILROAD:
                this.onRailroadNameMessage(type, data, request);
                break;
            default:
                this.connection.sendMessage(this.service.doPost(type, name, data, request), request.id);
                break;
        }
    }

    private void onRailroadNameMessage(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        String name = data.path(JSON.NAME).asText();
        // Follow up handling a POST (change railroad name command) with the same answer as a GET (ask)
        if (request.method.equals(JSON.POST)) {
            log.debug("Processing change railroad name to {} from socket service", name);
            this.connection.sendMessage(this.service.doPost(type, name, data, request), request.id);
        }
        this.connection.sendMessage(this.service.doGet(type, name, data, request), request.id);
        this.rrNameListener = (PropertyChangeEvent evt) -> {
            try {
                this.handleRailroadChange();
            } catch (IOException ex) {
                InstanceManager.getDefault(WebServerPreferences.class).removePropertyChangeListener(this.rrNameListener);
            }
        };
        InstanceManager.getOptionalDefault(WebServerPreferences.class).ifPresent(preferences -> preferences.addPropertyChangeListener(this.rrNameListener));
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        this.connection.sendMessage(this.service.doGetList(type, data, request), request.id);
    }

    @Override
    public void onClose() {
        InstanceManager.getOptionalDefault(WebServerPreferences.class).ifPresent(preferences -> preferences.removePropertyChangeListener(this.rrNameListener));
    }

    private void handleRailroadChange() throws IOException {
        try {
            connection.sendMessage(service.doGet(JSON.RAILROAD, null, connection.getObjectMapper().createObjectNode(), new JsonRequest(this.connection.getLocale(), JSON.V5, JSON.GET, 0)), 0);
        } catch (JsonException ex) {
            this.connection.sendMessage(ex.getJsonMessage(), 0);
        }
    }
}
