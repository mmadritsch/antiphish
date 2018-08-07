/*global window, browser */

// enum for traffic light
var TrafficLight = {
  GRAY: 0,
  GREEN: 1,
  YELLOW: 2,
  RED: 3
};

// dictionaries of icons for toolbar button (traffic lights)
var grayIcons = {
    "16": "icons/gray_16.png",
    "32": "icons/gray_32.png",
    "48": "icons/gray_48.png"
};

var greenIcons = {
    "16": "icons/green_16.png",
    "32": "icons/green_32.png",
    "48": "icons/green_48.png"
};

var yellowIcons = {
    "16": "icons/yellow_16.png",
    "32": "icons/yellow_32.png",
    "48": "icons/yellow_48.png"
};

var redIcons = {
    "16": "icons/red_16.png",
    "32": "icons/red_32.png",
    "48": "icons/red_48.png"
};

// integer to store current tab id
var currentTabId = 0;

// dictionary to store all tab results
var tabResults = {};

/**
 * Gets a random number between [0,1) and starts a timer, which calls
 * the changeIcon() Method.
 */
/*function startTimer() {
    var rand = Math.random();
    window.setTimeout(changeIcon, 5000, rand);
    console.log("Timer started");
}*/

/**
 * Changes the toolbar icon according to the received result.
 * @param {Number} trafficLight - Result of the analysis from the server
 */
function changeIcon(trafficLight) {
    if(trafficLight === TrafficLight.GREEN) {
        browser.browserAction.setIcon({path: greenIcons});
        console.log("Changed icon to green");
    } else if(trafficLight === TrafficLight.YELLOW) {
        browser.browserAction.setIcon({path: yellowIcons});
        console.log("Changed icon to yellow");
    } else if (trafficLight === TrafficLight.RED) {
        browser.browserAction.setIcon({path: redIcons});
        console.log("Changed icon to red");
    } else {
        browser.browserAction.setIcon({path: grayIcons});
        console.log("Changed icon to gray");
    }
}

/**
 * Will be called whenever a resource of type "main_frame" will be requested
 * and sends the corresponding URL to the REST-Server for analysis.
 * @param {Object} requestDetails - Details about the current request
 */
function logURL(requestDetails) {
    console.log("Sending URL: " + requestDetails.url + " to Server for Analysis");
    
    console.log();
    
    // send XMLHttpRequest to server
    var xhr = new XMLHttpRequest();
    //xhr.open("GET", "http://192.168.0.102:8080/api/analyse?url=" + requestDetails.url, true);
    //xhr.open("GET", "http://localhost:8080/api/analyse?url=" + requestDetails.url, true);
    //xhr.open("POST", "http://192.168.0.102:8080/api/analyse", true);
    xhr.open("POST", "http://localhost:8080/api/analyse", true);
    
    // send the proper header information along with the request
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");

    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
            console.log("Received response from server: " + xhr.responseText);
            var serverResult = JSON.parse(xhr.responseText);
            
            if('score' in serverResult && 'trafficLight' in serverResult) {
                
                // save result of tab which requested the url
                tabResults[requestDetails.tabId] = serverResult;
                console.log("Saved result to tab id: " + requestDetails.tabId);
                
                console.log("Requested tab id: " + requestDetails.tabId);
                console.log("Current tab id: " + currentTabId);
                // change icon if requested tab was the current tab
                if(requestDetails.tabId === currentTabId) {
                    changeIcon(serverResult.trafficLight);
                }
            } else {
                changeIcon(-1);
            }
        } else if (xhr.status !== 200){
            console.log("HTTP-Error: " + xhr.status + " " + xhr.responseText);
        }
    };
    xhr.send(requestDetails.url);
}

/**
 * Will be called whenever the current active tab changes and updates
 * the result.
 * @param {Object} activeInfo - Details about the current active tab
 */
function handleTabActivated(activeInfo) {
    currentTabId = activeInfo.tabId;
    console.log("Current tab id: " + currentTabId);
    
    if(tabResults[currentTabId] !== undefined) {
        console.log("   result: " + tabResults[currentTabId].score);
        console.log("   traffic light: " + tabResults[currentTabId].trafficLight);
        
        changeIcon(tabResults[currentTabId].trafficLight);
    } else {
        console.log("   no result for that tab available");
        
        changeIcon(TrafficLight.GRAY);
    }
    
}

/**
 * Will be called whenever a tab was closed and deletes related result
 * from tabResults dictionary.
 * @param {Number} tabId - id of removed tab
 */
function handleTabRemoved(tabId) {
    console.log("Removed tab id: " + tabId);
    
    if(tabResults[tabId] !== undefined) {
        console.log("   result for that tab is available and will be removed");
        
        delete tabResults[tabId];
        console.log(tabResults);
    } else {
        console.log("   no result for that tab available");
    }
    
}

// listener for intercepting all requests for resources of type "main_frame"
browser.webRequest.onBeforeRequest.addListener(
    logURL,
    {urls: ["<all_urls>"], types: ["main_frame"]}
);

// on click listener for toolbar button (traffic light)
//browser.browserAction.onClicked.addListener(startTimer);

// tab switch listener for changing currently displayed result
browser.tabs.onActivated.addListener(handleTabActivated);

// tab listener for deleting stored result if tab was closed
browser.tabs.onRemoved.addListener(handleTabRemoved);