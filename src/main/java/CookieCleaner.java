import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
//import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;

import burp.api.montoya.proxy.http.InterceptedRequest;
import burp.api.montoya.proxy.http.ProxyRequestHandler;
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction;
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;


import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;



public class CookieCleaner implements BurpExtension, ProxyRequestHandler, ContextMenuItemsProvider {
    private List<String> removedCookies = new CopyOnWriteArrayList<>();
    private MontoyaApi api;
    private CentrePanel centrePanel;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        CookieScanEngine scanEngine = new CookieScanEngine(api);
        centrePanel = new CentrePanel(scanEngine);
        removedCookies = centrePanel.getRemovedCookieList();
        api.extension().setName("Cookie Cleaner");
        api.userInterface().registerContextMenuItemsProvider(this);
        api.proxy().registerRequestHandler(this);
        api.userInterface().registerSuiteTab("Cookie Cleaner", centrePanel);
    }

    public ProxyRequestReceivedAction handleRequestReceived(InterceptedRequest interceptedRequest){

        if(!centrePanel.getActiveCleanVal()){
            return ProxyRequestReceivedAction.continueWith(interceptedRequest);
        }


        HttpRequest filtered = filterCookies(interceptedRequest, removedCookies);
        return ProxyRequestReceivedAction.continueWith(filtered);
    }

    @Override
    public ProxyRequestToBeSentAction handleRequestToBeSent(InterceptedRequest interceptedRequest) {
        //check if active clean is on. If not just send the request
        if(!centrePanel.getActiveCleanVal()){
            return ProxyRequestToBeSentAction.continueWith(interceptedRequest);
        }

        //send modified cookie header
        HttpRequest filtered = filterCookies(interceptedRequest, removedCookies);
        return ProxyRequestToBeSentAction.continueWith(filtered);
    }

    //Used for adding options to clean cookies
    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        List<Component> menuItems = new ArrayList<>();

        //Different options
        JMenuItem cleanCookies = new JMenuItem("Clean cookies");
        JMenuItem cleanCookiesPlusNewTab = new JMenuItem("Clean cookies + New tab");
        JMenuItem AddToRemovedAndClean = new JMenuItem("Add to removed cookies and clean");

        //Clean the request with specified cookies
        cleanCookies.addActionListener(e -> {
            event.messageEditorRequestResponse().ifPresent(editor -> {
                HttpRequest cleaned = filterCookies(editor.requestResponse().request(), removedCookies);
                    editor.setRequest(cleaned);
            });
        });
        menuItems.add(cleanCookies);

        //If not in Proxy/interpt, Do not show this option. (This creates a new repeater tab with the removed cookies)
        if(event.toolType() != ToolType.PROXY){
            cleanCookiesPlusNewTab.addActionListener(e -> {
                event.messageEditorRequestResponse().ifPresent(editor -> {
                    HttpRequest cleaned = filterCookies(editor.requestResponse().request(), removedCookies);
                    api.repeater().sendToRepeater(cleaned);
                });
            });
            menuItems.add(cleanCookiesPlusNewTab);
        }


        //This option only shows when text is highlighted.
        event.messageEditorRequestResponse().ifPresent(editor -> {
            editor.selectionOffsets().ifPresent(selection -> {
                //get the selected text
                byte[] request = editor.requestResponse().request().toByteArray().getBytes();
                String selectedText = new String(request, selection.startIndexInclusive(),
                        selection.endIndexExclusive() - selection.startIndexInclusive());

                //Check if multiple cookies
                String[] cookies = selectedText.split("; ");
                //If only one cookie is selected remove it.
                if(cookies.length == 1){
                    String cookie = cookies[0].split("=")[0];
                    AddToRemovedAndClean.addActionListener(e -> {
                        centrePanel.addOrRemove(cookie);
                        HttpRequest cleaned = filterCookies(editor.requestResponse().request(), removedCookies);
                        api.repeater().sendToRepeater(cleaned);
                    });
                }else{
                    //if multiple cookies are selected
                    for(String fullCookie : cookies){
                        String cookie = fullCookie.split("=")[0];
                        centrePanel.addOrRemove(cookie);
                    }
                    HttpRequest cleaned = filterCookies(editor.requestResponse().request(), removedCookies);
                    api.repeater().sendToRepeater(cleaned);
                }
                menuItems.add(AddToRemovedAndClean);
            });
        });

        return menuItems;
    }

    //Filters out the removed cookies
    private HttpRequest filterCookies(HttpRequest request, List<String> removedCookies) {
        String cookieHeader = request.headerValue("Cookie");
        if (cookieHeader == null) return request;

        String newCookieHeader = Arrays.stream(cookieHeader.split("; "))
                .filter(cookie -> !removedCookies.contains(cookie.split("=")[0]))
                .collect(Collectors.joining("; "));

        return request.withUpdatedHeader("Cookie", newCookieHeader);
    }
}
