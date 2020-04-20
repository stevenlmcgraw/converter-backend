package com.slowdraw.converterbackend.assembler;

import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ResultHistoryAssembleLinksForDeleteMethods {


    private Map<String, String> messageMap = new HashMap();
    private List<Link> linksList = new ArrayList<>();
    private List<Object> bodyList = new ArrayList<>();

    public Map<String, String> makeMessage(String username, String id) {

        String message = String.format("Username %s ResultHistory ID %s deleted.", username, id);

        messageMap.clear();

        messageMap.put("message", message);

        return messageMap;
    }

    public Map<String, String> makeMessage(String username) {

        String message = String.format("Username %s all ResultHistory deleted.", username);

        messageMap.clear();

        messageMap.put("message", message);

        return messageMap;
    }

    public Map<String, List<Link>> makeLinksList(String username) {

        Map<String, List<Link>> map = new HashMap<>();

        linksList.clear();

        linksList.add(new Link("http://localhost/resultHistory/" + username)
                .withRel("getAllUsernameResultHistory"));

        linksList.add(new Link("http://localhost/resultHistory")
                .withRel("saveSingleResultHistory"));

        linksList.add(new Link("http://localhost/resultHistory/delete/" +
                username + "/{id}")
                .withRel("deleteSpecificResultHistory"));

        linksList.add(new Link("http://localhost/resultHistory/delete/" + username)
                .withRel("deleteAllUsernameResultHistory"));

        map.put("_links", linksList);

        return map;
    }

    public List<Object> getBody(String username, String id) {

        bodyList.clear();

        bodyList.add(makeMessage(username, id));

        bodyList.add(makeLinksList(username));

        return bodyList;
    }

    public List<Object> getBody(String username) {

        bodyList.clear();

        bodyList.add(makeMessage(username));

        bodyList.add(makeLinksList(username));

        return bodyList;
    }
}
