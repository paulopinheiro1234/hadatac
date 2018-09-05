package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;

import org.hadatac.utils.NameSpace;

public class NamespaceForm {

    public List<NameSpace> namespaces = new ArrayList<NameSpace>();
 
    public NamespaceForm () {}
    
    public List<NameSpace> getNamespaces() {
        return namespaces;
    }
    
    public void setNamespaces(List<NameSpace> namespaces) {
        this.namespaces = namespaces;
    }
}
