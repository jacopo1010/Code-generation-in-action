package it.jacopo.www.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class MetaField {

	private String id;
	private String name;
	private String type;
	private String originalType;
	private String javaType;
	private String sqlType;
	private String label;
	private String widget;
	private String javaDoc;
	private String since;
	private String ownerLowerBound;
	private String ownerUpperBound;
	private String targetLowerBound;
	private String targetUpperBound;
	private String relationType;
	private String foreignKeyColumn;
	private boolean relation;
	private boolean collection;
	private boolean joinTableRequired;
	private boolean required;
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

	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public String getSqlType() {
		return sqlType;
	}

	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
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

	public String getJavaDoc() {
		return javaDoc;
	}

	public void setJavaDoc(String javaDoc) {
		this.javaDoc = javaDoc;
	}

	public String getSince() {
		return since;
	}

	public void setSince(String since) {
		this.since = since;
	}

	
	public String getOwnerLowerBound() {
		return ownerLowerBound;
	}

	public void setOwnerLowerBound(String ownerLowerBound) {
		this.ownerLowerBound = ownerLowerBound;
	}

	public String getOwnerUpperBound() {
		return ownerUpperBound;
	}

	public void setOwnerUpperBound(String ownerUpperBound) {
		this.ownerUpperBound = ownerUpperBound;
	}

	public String getTargetLowerBound() {
		return targetLowerBound;
	}

	public void setTargetLowerBound(String targetLowerBound) {
		this.targetLowerBound = targetLowerBound;
	}

	public String getTargetUpperBound() {
		return targetUpperBound;
	}

	public void setTargetUpperBound(String targetUpperBound) {
		this.targetUpperBound = targetUpperBound;
	}

	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

	public String getForeignKeyColumn() {
		return foreignKeyColumn;
	}

	public void setForeignKeyColumn(String foreignKeyColumn) {
		this.foreignKeyColumn = foreignKeyColumn;
	}

	public boolean isRelation() {
		return relation;
	}

	public void setRelation(boolean relation) {
		this.relation = relation;
	}

	public boolean isCollection() {
		return collection;
	}

	public void setCollection(boolean collection) {
		this.collection = collection;
	}

	public boolean isJoinTableRequired() {
		return joinTableRequired;
	}

	public void setJoinTableRequired(boolean joinTableRequired) {
		this.joinTableRequired = joinTableRequired;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
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
