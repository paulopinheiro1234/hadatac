package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TreeNode {

    private String name;
    private boolean firstVisit;
    private ArrayList<TreeNode> children;
    
    public TreeNode(String n) {
        name = n;
	firstVisit = true;
        children = new ArrayList<TreeNode>();
    }
 
    public List<TreeNode> getChildren() {
        return children;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean getFirstVisit() {
        return firstVisit;
    }
    
    public void setFirstVisit(boolean firstVisit) {
        this.firstVisit = firstVisit;
    }
    
    public TreeNode hasValue(String s) {
    	if (name.equals(s)) {
    		return this;
    	} else {
    		for (TreeNode child : children) {
    			TreeNode tempNode = child.hasValue(s);
    			if (tempNode != null) {
    				return tempNode;
    			}

    		}
    		return null;
    	}
    		
    }
    
    public void addChild(String s) {
    	TreeNode newTreeNode = new TreeNode(s);
    	children.add(newTreeNode);
    }
    
    public void addChild(TreeNode n) {
    	children.add(n);
    }
    
    public void replaceChild(TreeNode child, TreeNode replacement) {
    	if (children.contains(child)){
    		children.remove(child);
    		children.add(replacement);
    	} else {
    		System.out.println("TreeNode.java - replaceChild : Specified Child Not Contained in TreeNode Children: \n" + this.toJson(0) + "\n");
    	}
    }
    
    public String toJson(int level) {

    	String ind = "";
    	String json_children = "";

    	for (int i=0; i < level; i++) {
    		ind = ind + "   ";
    	}
    	
    	if (children.size() == 0) {
    		return(ind + "{ \"name\": \"" + name + "\" , \"size\": 10 }");
    	}
    	
    	json_children = "\n" + ind + "  \"children\": [\n";
    	boolean firstTime = true;

    	Iterator<TreeNode> nodeIterator = children.iterator();
		while (nodeIterator.hasNext()) {
			if (!firstTime) {
				json_children = json_children + ",\n";
			} else {
				json_children = json_children + "\n";
			}
    		json_children = json_children + nodeIterator.next().toJson(level + 1);
    		firstTime = false;
    	}
    	json_children = json_children + ind + "]\n";

    	return (ind + "{\n" + 
    			ind + "  \"name\": \"" + name + "\" ,"  +
    			json_children + 
    			ind + "}");	    	
    }
    
    public String toHtml(int level) {

    	String ind = "";
    	String json_children = "";

    	for (int i=0; i < level; i++) {
    		ind = ind + "   ";
    	}
    	
    	if (children.size() == 0) {
	    return(ind + " <a href=\"" + name + "\"></a> ");
    	}
    	
    	json_children = "<br>" + ind + " <ul><br>";
    	boolean firstTime = true;

    	Iterator<TreeNode> nodeIterator = children.iterator();
		while (nodeIterator.hasNext()) {
			if (!firstTime) {
				json_children = json_children + "<br>";
			} else {
				json_children = json_children + "<br>";
			}
    		json_children = json_children + nodeIterator.next().toHtml(level + 1);
    		firstTime = false;
    	}
    	json_children = json_children + ind + "</ul><br>";

    	return (ind + "<ul> " + 
    			ind + " <a href=\"" + name + "\"></a> "  +
    			json_children + 
    			ind + " </ul>");	    	
    }
    
}
