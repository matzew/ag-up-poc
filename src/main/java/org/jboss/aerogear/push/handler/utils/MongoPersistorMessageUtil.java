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

package org.jboss.aerogear.push.handler.utils;

import org.vertx.java.core.json.JsonObject;

/**
 * Incomplete util to make communicating with the mongo persistor easier....
 */
public final class MongoPersistorMessageUtil {
    
    private MongoPersistorMessageUtil () {
        // noop
    }

    public static JsonObject findMessageForIdAndCollection(String collection, String pushAppId) {
        JsonObject matcher = new JsonObject().putString("_id", pushAppId); 
        JsonObject eventBusMessage = new JsonObject().putString("collection", collection).putString("action", "find").putObject("matcher", matcher);

        return eventBusMessage;
    }

    public static JsonObject saveOrUpdateMessageForDocumentAndCollection(String collection, JsonObject object) {
        // yes... try update... you will see - does not really work...... so, doing save... (which does override)... Moving on.......
        JsonObject eventBusMsg = new JsonObject().putString("collection", collection).putString("action", "save").putObject("document", object);
        return eventBusMsg;
    }
    
}
