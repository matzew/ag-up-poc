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
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

/**
 * Handler that receives some details of a given <code>PushApplication</code>.
 */
public final class PushApplicationDetailsHandler implements Handler<HttpServerRequest> {
    private final EventBus eb;

    public PushApplicationDetailsHandler(EventBus eb) {
        this.eb = eb;
    }

    public void handle(final HttpServerRequest request) {
        
        // submitted on the URI... (see Server.java)
        final String pushAppId = request.params().get("pushAppId");


        // the MONGO ID is the 'pushAppId'   !!!!!!!!!!
        JsonObject ebMessage = MongoPersistorMessageUtil.findMessageForIdAndCollection("pushApplication", pushAppId);
        eb.send("vertx.mongopersistor", ebMessage, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                // should be only one.... ;-)
                JsonObject obj = (JsonObject) reply.body.getArray("results").get(0);
                
                // render the response:
                JsonObject pushAppDetails = new JsonObject();
                pushAppDetails
                    .putString("id", obj.getString("_id"))
                    .putString("name", obj.getString("name"))
                    .putString("description", obj.getString("description"));
                
                request.response.end(pushAppDetails.encode());
            }
        });
    }
}