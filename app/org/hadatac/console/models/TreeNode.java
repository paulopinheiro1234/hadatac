package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.Iterator;

public class TreeNode {

    private String name;
    private ArrayList<TreeNode> children;
    
    public TreeNode(String n) {
        name = n;
        children = new ArrayList<TreeNode>();
    }
 
    public ArrayList<TreeNode> getChildren() {
        return children;
    }
    
    public String getName() {
        return name;
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
    

}
