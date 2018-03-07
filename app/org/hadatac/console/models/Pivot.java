package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;

import play.libs.Json;

public class Pivot {
    public List<Pivot> children;

    public String field;
    public String value;
    public String tooltip;
    public int count;

    public Pivot() {
        children = new ArrayList<Pivot>();
    }

    public void addChild(Pivot child) {
        children.add(child);
    }

    public int recomputeStats() {
        if (children.isEmpty()) {
            return count;
        }

        int cnt = 0;
        for (Pivot child : children) {
            cnt += child.recomputeStats();
        }

        count = cnt;

        return count;
    }

    @Override
    public boolean equals(Object o) {
        if((o instanceof Pivot) && (((Pivot)o).value.equals(this.value))) {
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static Pivot parseQueryResponse(QueryResponse response) {
        if (response.getResponse() != null) {
            if (response.getResponse().get("facets") instanceof NamedList) {
                return parseNamedList(((NamedList<Object>)response.getResponse().get("facets")), "");
            }
        }

        return null;
    }

    private static Pivot parseNamedList(NamedList<Object> objects, String field) {
        Pivot pivot = new Pivot();
        objects.forEach(new BiConsumer<String, Object>() {

            @SuppressWarnings("unchecked")
            @Override
            public void accept(String t, Object u) {
                if (t.equals("val")) {
                    if (u instanceof String) {
                        pivot.value = (String)u;
                    } else {
                        pivot.value = u.toString();
                    }
                } else if (t.equals("count")) {
                    pivot.field = field;
                    pivot.count = (int)u;
                } else {
                    if (u instanceof ArrayList<?>) {
                        for (NamedList<Object> nl : (ArrayList<NamedList<Object>>) u) {
                            Pivot child = parseNamedList((NamedList<Object>)nl, field);
                            pivot.addChild(child);
                        }
                    } else if (u instanceof NamedList<?>) {
                        for (NamedList<Object> nl : ((ArrayList<NamedList<Object>>)((NamedList<Object>)u).get("buckets"))) {
                            Pivot child = parseNamedList((NamedList<Object>)nl, t);
                            pivot.addChild(child);
                        }
                    }
                }
            }
        });

        return pivot;
    }
    
    @Override
    public String toString() {
        return Json.toJson(this).toString();
    }
}
