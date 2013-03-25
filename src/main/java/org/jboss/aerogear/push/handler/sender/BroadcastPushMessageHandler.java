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

import org.jboss.aerogear.push.handler.utils.MongoPersistorMessageUtil;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

/**
 * Handler that receives push message payloads and submits them 
 * to a queue, which itself takes care of the actual delivery
 * to the different (configured/enabled) push networks.
 */
public final class BroadcastPushMessageHandler implements Handler<HttpServerRequest> {
    private final EventBus eb;
    // innerclass level:
    JsonObject pushMessage = null;

    public BroadcastPushMessageHandler(EventBus eb) {
        this.eb = eb;
    }

    @Override
    public void handle(final HttpServerRequest request) {
        // submitted on the URI... (see Server.java)
        final String pushAppId = request.params().get("pushAppId");

        request.dataHandler(new Handler<Buffer>() {
            public void handle(Buffer buffer) {
                
                // parse the incoming request data.... 
                // No checks are done........
                
                // Since the EventBus ONLY accepts JsonObject's - we only use them...
                // in the POC it does not make sense to transform from JsonObject to
                // "rich object graph" and vice versa... 
                // At some point a more richer API ... (e.g. ORM) would be nicer......  
                
                pushMessage = new JsonObject(buffer.toString());
            }
        });


        // the MONGO ID is the 'pushAppId'   !!!!!!!!!!
        JsonObject eventBusMessage = MongoPersistorMessageUtil.findMessageForIdAndCollection("pushApplication", pushAppId);
        eb.send("vertx.mongopersistor", eventBusMessage, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                // should be only one :)
                JsonObject pushAppWithMobileApps = (JsonObject) reply.body.getArray("results").get(0);

                // Create a 'container' object that contains the following:
                // * the actual payload of the message 
                // * all iOS application (incl. refs to their registered installations)
                // * all android application (incl. refs to their registered installations)

                JsonObject container = new JsonObject();

                // the message payload:
                container.putObject("payload", pushMessage);

                // query and add all relevant apps... sure... it could be done in a cooler fashion...
                container.putArray("iOS", pushAppWithMobileApps.getArray("iOS"));
                container.putArray("android", pushAppWithMobileApps.getArray("android"));

                // The container object is submitted to a 'global' queue, which takes care
                // of handing the message over to other queues (for the relevant push networks)
                // See GlobalSenderQueueHandler.java
                eb.send("aerogear.push.messages", container);
                request.response.end("Submitted for delivery"); // best response, ever!
            }
        });
    }
}