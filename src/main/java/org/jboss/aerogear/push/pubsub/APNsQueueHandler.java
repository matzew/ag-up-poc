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

import java.util.HashSet;
import java.util.Set;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

//import com.notnoop.apns.APNS;
//import com.notnoop.apns.ApnsService;

/**
 * Queue that receives messages for different apps/iOS views and 
 * submits them to the Apple Push Network...
 */
public final class APNsQueueHandler implements
        Handler<Message<JsonObject>> {
//    private ApnsService service = null;

    @Override
    public void handle(Message<JsonObject> event) {
		/**
        JsonObject payload     = event.body.getObject("payload");
        JsonArray applications = event.body.getArray("applications");

        for (Object app : applications) {
          JsonObject iOSApp = (JsonObject) app;
          String passphrase = iOSApp.getString("passphrase");
          String certPath = iOSApp.getString("certificate");

          JsonArray instances = iOSApp.getArray("instances");
          final Set<String> iOStokenz = new HashSet<String>();
          for (Object ob : instances) {
              JsonObject instance = (JsonObject) ob;
              iOStokenz.add(instance.getString("token"));
          }

          // could/should be extracted into something else... 
          // and not created in this loop....
          // PERHAPS.... EVERY iOS view should have its own (generated???) "Queue"
          // But for now, we loop over .....
          service = APNS.newService()
                .withCert(certPath, passphrase)
                .withSandboxDestination()
                .asQueued()
                .build();

          String msg = APNS.newPayload()
                  .alertBody(payload.getString("alert")) // payload from the message....
                  .badge(2)  // could submitted, on the payload - but hard coded for testing...
                  .sound("default")  // could submitted, on the payload - but hard coded for testing...
                  .build();
          service.push(iOStokenz, msg);
        }
	  **/
    }
}