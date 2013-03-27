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

import java.util.UUID;

import org.jboss.aerogear.push.handler.utils.MongoPersistorMessageUtil;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Handler that creates the logical construct of a <code>MobileApplication</code>.
 */
public final class MobileApplicationRegistrationHandler implements
        Handler<HttpServerRequest> {

    private final EventBus eb;
    private JsonObject newMobileApplicationDocument;

    public MobileApplicationRegistrationHandler(EventBus eb) {
        this.eb = eb;
    }

    @Override
    public void handle(final HttpServerRequest request) {
        // submitted on the URI patterns:... (see Server.java)

        final String pushAppId = request.params().get("pushAppId");
        // TDOD check for 'allowed' variant...
        final String variant = request.params().get("variant");

        // end here - if unsupported request occurs 
        if (! notAllowedVariant(variant)) {
            request.response.statusCode = 500;
            request.response.end("Not allowed");
            return;
        }

        request.dataHandler(new Handler<Buffer>() {
            public void handle(Buffer buffer) {
                
                // parse the incoming request data.... 
                // No checks are done........
                
                // Since the EventBus ONLY accepts JsonObject's - we only use them...
                // in the POC it does not make sense to transform from JsonObject to
                // "rich object graph" and vice versa... 
                // At some point a more richer API ... (e.g. ORM) would be nicer......  
                
                newMobileApplicationDocument = new JsonObject(buffer.toString());

                // generate mobile app ID:
                String aeroGearMobileAppId = UUID.randomUUID().toString();
                
                newMobileApplicationDocument.putString("aeroGearMobileAppId", aeroGearMobileAppId);
            }
        });

        // look up for the desired push app:
        JsonObject ebMessage = MongoPersistorMessageUtil.findMessageForIdAndCollection("pushApplication", pushAppId);
        eb.send("vertx.mongopersistor", ebMessage, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                // should be only one...
                JsonObject pushApplication = (JsonObject) reply.body.getArray("results").get(0);
                
                JsonArray mobileApplicationsForGivenVariant = pushApplication.getArray(variant);
                // checker...
                if (mobileApplicationsForGivenVariant == null) {
                    mobileApplicationsForGivenVariant = new JsonArray();
                }
                // ADD THE NEW APP:
                mobileApplicationsForGivenVariant.addObject(newMobileApplicationDocument);
                
                // (re) add the reference...:
                pushApplication.putArray(variant, mobileApplicationsForGivenVariant);
                
                // update the push app, with the new mobile variant....
                JsonObject eventBusMsg = MongoPersistorMessageUtil.saveOrUpdateMessageForDocumentAndCollection("pushApplication", pushApplication);
                eb.send("vertx.mongopersistor", eventBusMsg, new Handler<Message<JsonObject>>() {
                    @Override
                    public void handle(Message<JsonObject> event) {
                        // noop.... TODO: check is save/update really really worked...................
                    }
                });


                // render the new mobile application ID....
                JsonObject resp = new JsonObject().putString("aeroGearMobileAppId", newMobileApplicationDocument.getString("aeroGearMobileAppId"));
                request.response.end(resp.encode());
            }
        });
    }

    private boolean notAllowedVariant(String variant) {
        if ("web".equals(variant) || "iOS".equals(variant) || "android".equals(variant)  ) {
            return true;
        }
        
        // nope
        return false;
    }
}