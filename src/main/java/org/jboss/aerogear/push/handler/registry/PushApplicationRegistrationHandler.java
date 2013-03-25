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

package org.jboss.aerogear.push.handler.registry;

import org.jboss.aerogear.push.handler.utils.MongoPersistorMessageUtil;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

/**
 * Handler that creates the logical construct of a <code>PushApplication</code>.
 */
public final class PushApplicationRegistrationHandler implements
        Handler<HttpServerRequest> {
    private final EventBus eb;

    public PushApplicationRegistrationHandler(EventBus eb) {
        this.eb = eb;
    }

    public void handle(final HttpServerRequest request) {
        request.dataHandler(new Handler<Buffer>() {
            
            @Override
            public void handle(Buffer buffer) {
                
                // parse the incoming request data.... 
                // No checks are done........
                
                // Since the EventBus ONLY accepts JsonObject's - we only use them...
                // in the POC it does not make sense to transform from JsonObject to
                // "rich object graph" and vice versa... 
                // At some point a more richer API ... (e.g. ORM) would be nicer......  
                
                JsonObject newPushApplicationJsonDocument = new JsonObject(buffer.toString());

                // Event Bus message, to be submitted to the Mongo Persistor Module:
                JsonObject eventBusMsg = MongoPersistorMessageUtil.saveOrUpdateMessageForDocumentAndCollection("pushApplication", newPushApplicationJsonDocument);
                eb.send("vertx.mongopersistor", eventBusMsg, new Handler<Message<JsonObject>>() {
                    @Override
                    public void handle(Message<JsonObject> reply) {
                        
                        // check, if it could be stored... 
                        if (reply.body.getString("status").equalsIgnoreCase("ok")) {

                            // ... and render the response....... 
                            // in Mongo... the PushAppID is the 'native' _id:
                            JsonObject response = new JsonObject().putString("id", reply.body.getString("_id"));
                            request.response.statusCode = HttpResponseStatus.CREATED.getCode();
                            request.response.end(response.encode());
                        } else {
                            // TODO...
                        }
                    }
                });
            }
        });
    }
}