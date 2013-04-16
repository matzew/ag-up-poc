/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.aerogear.push.handler.sender;

import java.util.Properties;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hornetq.api.jms.HornetQJMSClient;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class WebNotificationHandler implements Handler<HttpServerRequest> {
    private Connection connection = null;
    private InitialContext initialContext = null;
    private Session session = null;

    public WebNotificationHandler() {
        // ugly
        Properties props = new Properties();
        props.put("java.naming.factory.initial","org.jnp.interfaces.NamingContextFactory");
        props.put("java.naming.provider.url", "jnp://localhost:1099");
        props.put("java.naming.factory.url.pkgs","org.jboss.naming:org.jnp.interfaces");
        try {
            initialContext =  new InitialContext(props);
        } catch (NamingException e) {
            e.printStackTrace();
        }

        
        // ugly too:
        try {
            ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("/ConnectionFactory");
            connection = cf.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();
        } catch (NamingException e) {
        } catch (JMSException jmse) {
            jmse.printStackTrace();
        }
    }
    
    
    @Override
    public void handle(HttpServerRequest request) {
        request.dataHandler(new Handler<Buffer>() {
            public void handle(Buffer buffer) {

                // parse the incoming request data.... 
                // No checks are done........

                // Since the EventBus ONLY accepts JsonObject's - we only use them...
                // in the POC it does not make sense to transform from JsonObject to
                // "rich object graph" and vice versa... 
                // At some point a more richer API ... (e.g. ORM) would be nicer......  

                JsonObject pushMessage = new JsonObject(buffer.toString());
                
                JsonArray clients = pushMessage.getArray("clients");
                
                for (Object object : clients) {
                    String clientID = object.toString();
                    
                    JsonArray payload = pushMessage.getArray("payload");
                    
                    Topic topic   = HornetQJMSClient.createTopic("aerogear." + clientID);
                    
                    for (Object payloadObjects : payload) {
                        JsonObject jsonObj = (JsonObject) payloadObjects;
                        
                        Set<String> endpointnames = jsonObj.getFieldNames();
                        
                        for (String endpoint : endpointnames) {
                            try {
                                MessageProducer producer = session.createProducer(topic);
                                TextMessage message = session.createTextMessage(jsonObj.getString(endpoint));

                                // stomp headers:
                                message.setStringProperty("endpoint", endpoint);
                                producer.send(message);
                            } catch (JMSException jmse) {
                                jmse.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        
        request.response.end("Submitted for delivery"); // best response, ever!
    }
}
