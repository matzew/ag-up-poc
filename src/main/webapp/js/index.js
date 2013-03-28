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

var notifier = AeroGear.Notifier({
    name: "stompClient",
    settings: {
        connectURL: window.location.protocol + '//' + window.location.hostname + ':' + window.location.port + '/eventbus',
        onConnect: function() {
            console.log('Connected');
        },
        onDisconnect: function() {
            console.log('Disconnected');
        },
        onConnectError: function() {
            console.log('Connect Error');
        }
    }
});

function channelCallback( msg ) {
    $("#listview").append("<li data-icon='delete' data-theme='" + msg.theme + "'><a href='#'>" + msg.alert + "</a></li>").listview("refresh");
}

// Message removal
$( "#listview" ).on( "click", "li", function( event ) {
    $( this ).remove();
    $("#listview").listview("refresh");
});

// Subscribe to Channel
$( "#channel-list" ).on( "click", ".add", function( event ) {
    var $this = $( this ),
        addCount = $("#available-channels .ui-li-count"),
        subCount = $("#subscribed-channels .ui-li-count");

    notifier.clients.stompClient.subscribe({
        address: $.trim( $this.text() ),
        callback: channelCallback
    });
    $this
        .toggleClass("add remove")
        .buttonMarkup({ icon: "delete"})
        .insertBefore("#available-channels");
    addCount.text( +addCount.text() - 1 );
    subCount.text( +subCount.text() + 1 );
    $("#channel-list").listview("refresh");
});

// Unsubscribe from Channel
$( "#channel-list" ).on( "click", ".remove", function( event ) {
    var $this = $( this ),
        addCount = $("#available-channels .ui-li-count"),
        subCount = $("#subscribed-channels .ui-li-count");

    notifier.clients.stompClient.unsubscribe({
        address: $.trim( $this.text() ),
        callback: channelCallback
    });
    $this
        .toggleClass("add remove")
        .buttonMarkup({ icon: "plus"})
        .appendTo("#channel-list");
    addCount.text( +addCount.text() + 1 );
    subCount.text( +subCount.text() - 1 );
    $("#channel-list").listview("refresh");
});
