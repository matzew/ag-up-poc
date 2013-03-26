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

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Global queue that receives ALL message requests.
 * It splits the different mobile push providers into separated queues...
 */
public final class GlobalSenderQueueHandler implements
        Handler<Message<JsonObject>> {
    private final EventBus eb;

    public GlobalSenderQueueHandler(EventBus eb) {
        this.eb = eb;
    }

    @Override
    public void handle(Message<JsonObject> event) {
        // hrm nasty (hard coded) hack... 
        // but the idea is to split....
        JsonObject messageContainer = event.body;
        JsonObject payload = messageContainer.getObject("payload");
        JsonArray iOSapps = messageContainer.getArray("iOS");
        JsonArray androidApps = messageContainer.getArray("android");

        /*
         *  1) APNs ..... dispatch 
         */
        
        JsonObject apns = new JsonObject();
        apns.putObject("payload", payload);
        if (iOSapps != null) {
            apns.putArray("applications",iOSapps);

            // hand off to apple batch processor..
            eb.send("aerogear.push.messages.ios", apns);
        }

        /*
         *  2) GCM ..... dispatch 
         */
        
        JsonObject gcm = new JsonObject();
        gcm.putObject("payload", payload);
        if (androidApps != null) {
            gcm.putArray("applications",androidApps);

            eb.send("aerogear.push.messages.android", gcm);
        }

        /*
         * 3) Web based push.... 
         */

        JsonObject webMessage = new JsonObject();
        webMessage.putString("text", payload.getString("alert"));
        eb.send("org.aerogear.messaging", webMessage, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                //System.out.println(event);
            }
        }); 

    }
}
