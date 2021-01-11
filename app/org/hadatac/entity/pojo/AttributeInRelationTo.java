package org.hadatac.entity.pojo;

public class AttributeInRelationTo {

    private Attribute attr;
    private Entity inRelationTo;
    public AttributeInRelationTo(Attribute attr, Entity inRelationTo) {
        this.attr = attr;
        this.inRelationTo = inRelationTo;
    }

    public String getKey() {
        return attr.getUri() + inRelationTo.getUri();
    }

    public Attribute getAttribute() {
        return attr;
    }

    public Entity getInRelationTo() {
        return inRelationTo;
    }

    public String toString() {
        if (inRelationTo.equals("")) {
            return attr.getLabel();
        } else {
            return attr.getLabel() + "-" + inRelationTo;
        }
    }

}
