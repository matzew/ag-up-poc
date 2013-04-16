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
 * Handler that registers a new installation of a certain <code>MobileApplication</code> app, from a device.
 * The handler parse the information submitted from the phone, which contains the installed application
 */
public final class NewInstallationRegistrationHandler implements
        Handler<HttpServerRequest> {
    private final EventBus eb;
    JsonObject newRegisteredInstallation = null;

    public NewInstallationRegistrationHandler(EventBus eb) {
        this.eb = eb;
    }

    @Override
    // TODO: force HTTPs for this endpoint
    public void handle(final HttpServerRequest request) {
        // Due to 'security' reasons.. the following IDs are submitted as headers...
        final String pushApplicationID = request.headers().get("AG-PUSH-APP");
        final String mobileApplicationID = request.headers().get("AG-Mobile-APP");

        request.dataHandler(new Handler<Buffer>() {
            public void handle(Buffer buffer) {
                
                // parse the incoming request data.... 
                // No checks are done........
                
                // Since the EventBus ONLY accepts JsonObject's - we only use them...
                // in the POC it does not make sense to transform from JsonObject to
                // "rich object graph" and vice versa... 
                // At some point a more richer API ... (e.g. ORM) would be nicer......  
                newRegisteredInstallation = new JsonObject(buffer.toString());
                
                // generate a "device/app" ID:
                String deviceAppAId = UUID.randomUUID().toString();
                newRegisteredInstallation.putString("id", deviceAppAId);
             
                
            }
        });

        JsonObject eventBusMessage = MongoPersistorMessageUtil.findMessageForIdAndCollection("pushApplication", pushApplicationID);
        eb.send("vertx.mongopersistor", eventBusMessage, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {

                // the push app - should be only one
                JsonObject pushApplication = (JsonObject) reply.body.getArray("results").get(0);

                // extract the desired variant.....
                // based on the specified OS (see request payload for this ENDPOINT)
                String variant = newRegisteredInstallation.getString("os");
                JsonArray mobileApplicationsForVariant = pushApplication.getArray(variant);

                JsonObject targetAppForNewInstallation = null;
                for (Object object : mobileApplicationsForVariant) {
                    JsonObject mobileApp = (JsonObject) object;

                    // see if the app key is matching!
                    if (mobileApp.getString("aeroGearMobileAppId").equals(mobileApplicationID)){
                        targetAppForNewInstallation = mobileApp;
                        break;
                    }
                }

                // get all registered installations, and add this new one...:
                JsonArray instances = targetAppForNewInstallation.getArray("instances");
                if (instances == null) {
                    instances = new JsonArray();
                    targetAppForNewInstallation.putArray("instances", instances);
                }
                // add it here:
                instances.addObject(newRegisteredInstallation);

                JsonObject eventBusMessage = MongoPersistorMessageUtil.saveOrUpdateMessageForDocumentAndCollection("pushApplication", pushApplication);
                eb.send("vertx.mongopersistor", eventBusMessage, new Handler<Message<JsonObject>>() {
                    @Override
                    public void handle(Message<JsonObject> event) {
                        // noop.... TODO: check is save/update really really worked...................
                    }
                });

                // hack:
                request.response.headers().put("Access-Control-Allow-Origin", "*");
                request.response.headers().put("Access-Control-Allow-Credentials", "true");
                request.response.headers().put("Access-Control-Allow-Headers", "Content-Type, AG-PUSH-APP, AG-Mobile-APP");
                
                // For now, we just render the new ID........ BUT.... we could do else........
                JsonObject resp = new JsonObject().putString("ID", newRegisteredInstallation.getString("id"));
                request.response.end(resp.encode());
            }
        });
    }
}
