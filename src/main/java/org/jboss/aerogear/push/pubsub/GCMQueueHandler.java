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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

//import com.google.android.gcm.server.Sender;

/**
 * Queue that receives messages for different apps/Android views and 
 * submits them to the Google Push Network...
 */
public final class GCMQueueHandler implements Handler<Message<JsonObject>> {
//    private Sender sender = null;

    @Override
    public void handle(Message<JsonObject> event) {
		/**
        JsonObject payload    = event.body.getObject("payload");
        JsonArray applications = event.body.getArray("applications");

        for (Object app : applications) {
          JsonObject androidApp = (JsonObject) app;
          String googleApiKey = androidApp.getString("google-api-key");
          
          JsonArray instances = androidApp.getArray("instances");
          final List<String> androidtokenz = new ArrayList<String>();
          
          for (Object ob : instances) {
              JsonObject instance = (JsonObject) ob;
              androidtokenz.add(instance.getString("token"));
          }


          // could/should be extracted into something else... 
          // and not created in this loop....
          // PERHAPS.... EVERY Android view should have its own (generated???) "Queue"
          // But for now, we loop over .....
          sender = new Sender(googleApiKey);
          com.google.android.gcm.server.Message msg =
                  new com.google.android.gcm.server.Message.Builder()
          
              .addData("text", payload.getString("alert")) // payload from the message....
              .addData("title", "FOOOOO") // could submitted, on the payload - but hard coded for testing... (no meaning, here...)
              .build();

          // send it out.....
          try {
              sender.send(msg, androidtokenz, 0);
          } catch (IOException e) {
              e.printStackTrace();
          }
        }
		**/
    }
}
