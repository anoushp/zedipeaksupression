package zedipeaksupression.framework;

import java.util.ArrayList;

import repast.simphony.annotate.AgentAnnot;

@AgentAnnot(displayName = "Agent")
public class Node {
	
	private String id; 
	private ArrayList<NodeProperty> properties;
	private ArrayList<? extends NodeController> controllers;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public ArrayList<NodeProperty> getProperties() {
		return properties;
	}
	public void setProperties(ArrayList<NodeProperty> properties) {
		this.properties = properties;
	}
	public ArrayList<? extends NodeController> getControllers() {
		return controllers;
	}
	public void setControllers(ArrayList<? extends NodeController> controllers) {
		this.controllers = controllers;
	}

}
