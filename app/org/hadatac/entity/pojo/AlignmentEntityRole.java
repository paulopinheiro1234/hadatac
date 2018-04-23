package org.hadatac.entity.pojo;

import org.hadatac.entity.pojo.Entity;

public class AlignmentEntityRole {

    private Entity ent;
    private String role;

    public AlignmentEntityRole(Entity ent, String role) {
	this.ent = ent;
	this.role = role;
    }

    public String getKey() {
	return role + ent.getLabel();
    }

    public Entity getEntity() {
	return ent;
    }

    public String getRole() {
	return role;
    }

    public String toString() {
	return role + "-" + ent.getLabel();
    }

}
