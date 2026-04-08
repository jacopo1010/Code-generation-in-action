package it.jacopo.www.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class MetaField {

	private String id;
	private String name;
	private String type;
	private String originalType;
	private String label;
	private String widget;
	private String lowerBound;
	private String upperBound;
	private boolean relation;
	private Map<String, String> tags;

	public MetaField() {
		this.tags = new LinkedHashMap<String, String>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOriginalType() {
		return originalType;
	}

	public void setOriginalType(String originalType) {
		this.originalType = originalType;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getWidget() {
		return widget;
	}

	public void setWidget(String widget) {
		this.widget = widget;
	}

	public String getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(String lowerBound) {
		this.lowerBound = lowerBound;
	}

	public String getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(String upperBound) {
		this.upperBound = upperBound;
	}

	public boolean isRelation() {
		return relation;
	}

	public void setRelation(boolean relation) {
		this.relation = relation;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	public void addTag(String key, String value) {
		this.tags.put(key, value);
	}
}
