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

package org.jboss.aerogear.push.api;

import java.util.List;

/**
 * One Web Application (regardless if mobile or not), can subscribe to a list of endpoints,
 * to receive messages/notifcations.
 * 
 * While WebSocket (and others) allow sending LARGE contents, a notification here is more like
 * <i>You have mail(tm)</i>
 */
public interface WebApplication extends MobileApplication {
    
    
    /**
     * The list of the endpoints that the web application is subscribed to... 
     */
    List<String> getEndpoints();
    void addEndpoint(String endpointName);
    void removeEndpoint(String endpointName);
}
