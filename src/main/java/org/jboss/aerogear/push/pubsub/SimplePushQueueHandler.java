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

package org.jboss.aerogear.push.pubsub;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class SimplePushQueueHandler implements Handler<Message<JsonObject>> {
    
    private Connection connection = null;
    private InitialContext initialContext = null;
    private Session session = null;

    public SimplePushQueueHandler() {
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
    public void handle(Message<JsonObject> event) {
        JsonObject payload    = event.body.getObject("payload");
        JsonArray applications = event.body.getArray("applications");

        for (Object app : applications) {
          JsonObject webApp = (JsonObject) app;
          
          // NOT USED, YET.....
          webApp.getArray("endpoints");
          
          // GLOBAL CHANNEL, NOW.....
          try {
              Topic topic   = (Topic)initialContext.lookup("/topic/chat");
              MessageProducer producer = session.createProducer(topic);
              TextMessage message = session.createTextMessage(payload.encode());
              producer.send(message);
          } catch (NamingException e) {
              //
          } catch (JMSException jmse) {
              jmse.printStackTrace();
          }
        }
    }
}
