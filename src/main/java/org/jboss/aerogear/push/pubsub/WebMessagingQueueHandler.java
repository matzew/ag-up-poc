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
 * Queue that receives messages for different (mobile) Web Applications and 
 * submits them to a SockJS queue/channel/topic...
 */
public final class WebMessagingQueueHandler implements Handler<Message<JsonObject>> {
    
    private final EventBus eb;
    
    public WebMessagingQueueHandler(EventBus eb) {
        this.eb = eb;
    }

    @Override
    public void handle(Message<JsonObject> event) {
        JsonObject payload    = event.body.getObject("payload");
        JsonArray applications = event.body.getArray("applications");

        for (Object app : applications) {
          JsonObject webApp = (JsonObject) app;
          
          JsonArray channels = webApp.getArray("channels");
          
          // broadcast to all the channels
          for (Object channel : channels) {
              eb.publish((String) channel, payload);
          }
        }
    }
}
