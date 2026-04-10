package it.jacopo.www.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class MetaClass {

	private String id;
	private String name;
	private String table;
	private String pageTitle;
	private String javaDoc;
	private String since;
	private String author;
	private Map<String, String> tags;
	private Map<String, MetaField> fields;
	private Map<String, MetaClass> classes;

	public MetaClass() {
		this.tags = new LinkedHashMap<String, String>();
		this.fields = new LinkedHashMap<String, MetaField>();
		this.classes = new LinkedHashMap<String, MetaClass>();
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

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
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

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	public Map<String, MetaField> getFields() {
		return fields;
	}

	public void setFields(Map<String, MetaField> fields) {
		this.fields = fields;
	}

	public Map<String, MetaClass> getClasses() {
		return classes;
	}

	public void setClasses(Map<String, MetaClass> classes) {
		this.classes = classes;
	}

	public void addTag(String key, String value) {
		this.tags.put(key, value);
	}

	public void addField(MetaField field) {
		this.fields.put(field.getName(), field);
	}

	public void addClass(MetaClass metaClass) {
		this.classes.put(metaClass.getName(), metaClass);
	}
}
