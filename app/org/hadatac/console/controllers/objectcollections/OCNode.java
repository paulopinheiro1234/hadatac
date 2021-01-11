package org.hadatac.console.controllers.objectcollections;

import java.util.List;

public class OCNode {

    public static final int STUDY = 0;
    public static final int COLLECTION = 1;
    public static final int SPACECOLLECTION = 2;
    public static final int TIMECOLLECTION = 3;
    public static final int OBJECT = 1;
    public static final int SPACEOBJECT = 2;
    public static final int TIMEOBJECT = 3;
    public static final int PROPERTY = 4;
    public static final int VARIABLE = 5;
    public static final int SOC = 6;
    public static final int ONTOLOGY = 7;
    public static final int INDICATOR = 8;
    public static final int DEPLOYMENT = 9;
    public static final int SDD = 10;
    public static final int DASPEC = 11;

    private String name;
    private String uri;
    private int type;
    private String htmlDoc;
    private List<String> memberOf;

    public OCNode(String n, String u , int t, String hd, List<String> mOf) {
        name = n;
        uri = u;
        type = t;
        htmlDoc = hd;
        memberOf = mOf;
    }

    public String getName() {
        return name;
    }

    public String getURI() {
        return uri;
    }

    public int getType() {
        return type;
    }

    public String getHtmlDoc() {
        return htmlDoc;
    }

    public List<String> getMemberOf() {
        return memberOf;
    }

    public void addMember(String memberName) {
        memberOf.add(memberName);
    }



}
