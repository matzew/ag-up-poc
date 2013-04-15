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

package org.jboss.aerogear.push;

import org.jboss.aerogear.push.handler.registry.MobileApplicationRegistrationHandler;
import org.jboss.aerogear.push.handler.registry.NewInstallationRegistrationHandler;
import org.jboss.aerogear.push.handler.registry.PushApplicationDetailsHandler;
import org.jboss.aerogear.push.handler.registry.PushApplicationRegistrationHandler;
import org.jboss.aerogear.push.handler.sender.BroadcastPushMessageHandler;
import org.jboss.aerogear.push.pubsub.APNsQueueHandler;
import org.jboss.aerogear.push.pubsub.GCMQueueHandler;
import org.jboss.aerogear.push.pubsub.GlobalSenderQueueHandler;
import org.jboss.aerogear.push.pubsub.SimplePushQueueHandler;
import org.jboss.aerogear.push.pubsub.WebMessagingQueueHandler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Verticle;

/**
 * The server....
 */
public class Server extends Verticle{

    @Override
    public void start() throws Exception {

        /* Some initial stuff, setting up the infrastructure  */
        
        // create the http server
        HttpServer server = vertx.createHttpServer();
        
        // get EventBus...
        final EventBus eb = vertx.eventBus(); 
        
        
        /* The PUB/SUB System for native Push Messages   */

        // QUEUES for the actual push messaging....:
        eb.registerHandler("aerogear.push.messages", new GlobalSenderQueueHandler(eb));
        eb.registerHandler("aerogear.push.messages.ios", new APNsQueueHandler());
        eb.registerHandler("aerogear.push.messages.android", new GCMQueueHandler());
//        eb.registerHandler("aerogear.push.messages.web", new WebMessagingQueueHandler(eb));
        eb.registerHandler("aerogear.push.messages.web", new SimplePushQueueHandler());


        // REST API Endpoints....
        
        // ================= PushApplicationRegistry =================  
        
        RouteMatcher rm = new RouteMatcher();

        /* THE API SERVER HAS THE FOLLOWING ENDPOINTS */
        
        // register Push Applications:
        rm.post("/applications", new PushApplicationRegistrationHandler(eb));
        // Get PushApplications Details:
        rm.get("/applications/:pushAppId", new PushApplicationDetailsHandler(eb));
        // Register Mobile Apps:
        rm.post("/applications/:pushAppId/:variant", new MobileApplicationRegistrationHandler(eb));
        // register token from the user/device
        rm.post("/registry/device", new NewInstallationRegistrationHandler(eb));


        // ================= SENDER API ENDPOINT(S) =================  

        // Broadcast messages
        rm.post("/sender/broadcast/:pushAppId", new BroadcastPushMessageHandler(eb));



        // ================= POOR MAN's WebServer    =================  
        // static files:
        rm.getWithRegEx(".*", new Handler<HttpServerRequest>() {
            public void handle(final HttpServerRequest req) {
                if (req.uri.matches("/")) {
                    // MEH!!!
                    req.response.sendFile("src/main/webapp/index.html");
                } else {
                    // MEH !!!
                    req.response.sendFile("src/main/webapp/" + req.path);
                }
            }
        });


        // deploy the modules:
        JsonObject mongoConfig = new JsonObject();
        //mongoConfig.putString("db_name", "unified-push3");
        mongoConfig.putString("db_name", "unified-push4");

        // Not needed... but somewhere we need to store... for fun... mongo db....
        container.deployModule("vertx.mongo-persistor-v1.2", mongoConfig, 1);


        // add/deploy REST enpoints
        server.requestHandler(rm);



        // fire up the server
        server.listen(8080);
    }
}
