package it.jacopo.www.generator;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import it.jacopo.www.io.IO;
import it.jacopo.www.model.MetaClass;
import it.jacopo.www.model.MetaField;

public class GeneratoreDiMetaClass implements GeneratoreDiEntita{

	private DocumentBuilderFactory factory;
	private Map<String,MetaClass> metaDati;
	private IO io;
	
	public GeneratoreDiMetaClass(IO io) {
		this.io = io;
		this.factory = DocumentBuilderFactory.newInstance();
		this.factory.setNamespaceAware(true);
		this.metaDati = new LinkedHashMap<String, MetaClass>();
	}
	
	@Override
	public Map<String, MetaClass> generaMetaClass(File xml) {
		 try {
			DocumentBuilder builder = this.factory.newDocumentBuilder();
			Document doc = builder.parse(xml);
			doc.getDocumentElement().normalize();
			io.stampaMessaggio("Root element: " + doc.getDocumentElement().getNodeName());
			this.metaDati.clear();
			Map<String, String> dataTypes = this.extractDataTypes(doc);
			List<Element> classes = this.extractClassElements(doc);
			List<Element> associations = this.extractAssociationElements(doc);

			for (Element classElement : classes) {
				MetaClass metaClass = this.buildMetaClass(classElement, dataTypes, classes, associations);
				this.metaDati.put(metaClass.getName(), metaClass);
			}
			return new LinkedHashMap<String, MetaClass>(this.metaDati);
			
		 } catch (ParserConfigurationException e) {
			throw new RuntimeException("Errore nella configurazione del parser XML", e);
		 } catch (SAXException e) {
			throw new RuntimeException("Errore nel parsing del file XMI: " + xml.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new RuntimeException("Errore di lettura del file XMI: " + xml.getAbsolutePath(), e);
		}
	}

	public Map<String, MetaClass> getMetaDati() {
		return metaDati;
	}

	private Map<String, String> extractDataTypes(Document doc) {
		Map<String, String> dataTypes = new LinkedHashMap<String, String>();
		this.collectDataTypes(doc.getDocumentElement(), dataTypes);
		return dataTypes;
	}

	private void collectDataTypes(Element parent, Map<String, String> dataTypes) {
		if ("uml:DataType".equals(this.attr(parent, "xmi:type"))) {
			dataTypes.put(this.attr(parent, "xmi:id"), this.attr(parent, "name"));
		}

		List<Element> children = this.directChildren(parent);
		for (Element child : children) {
			this.collectDataTypes(child, dataTypes);
		}
	}

	private List<Element> extractClassElements(Document doc) {
		List<Element> classes = new ArrayList<Element>();
		this.collectClasses(doc.getDocumentElement(), classes);
		return classes;
	}
	
	private List<Element> extractAssociationElements(Document doc) {
		List<Element> associations = new ArrayList<Element>();
		this.collectAssociations(doc.getDocumentElement(), associations);
		return associations;
	}

	private void collectClasses(Element parent, List<Element> classes) {
		if ("uml:Class".equals(this.attr(parent, "xmi:type"))) {
			classes.add(parent);
		}

		List<Element> children = this.directChildren(parent);
		for (Element child : children) {
			this.collectClasses(child, classes);
		}
	}
	
	private void collectAssociations(Element parent, List<Element> associations) {
		if ("uml:Association".equals(this.attr(parent, "xmi:type"))) {
			associations.add(parent);
		}

		List<Element> children = this.directChildren(parent);
		for (Element child : children) {
			this.collectAssociations(child, associations);
		}
	}

	private MetaClass buildMetaClass(Element classElement, Map<String, String> dataTypes, List<Element> classes,
			List<Element> associations) {
		MetaClass metaClass = new MetaClass();
		metaClass.setId(this.attr(classElement, "xmi:id"));
		metaClass.setName(this.attr(classElement, "name"));

		Map<String, String> classTags = this.extractTags(classElement);
		metaClass.setTags(classTags);
		metaClass.setTable(classTags.get("table"));
		metaClass.setPageTitle(classTags.get("pageTitle"));
		metaClass.setJavaDoc(this.resolveDocumentation(classTags, metaClass.getName()));
		metaClass.setSince(classTags.get("since"));
		metaClass.setAuthor(classTags.get("author"));

		List<Element> attributes = this.directChildrenByName(classElement, "ownedAttribute");
		for (Element attribute : attributes) {
			MetaField metaField = this.buildAttribute(attribute, dataTypes, classes);
			metaClass.addField(metaField);
		}

		for (Element association : associations) {
			if (this.isAssociationOwnedByClass(association, metaClass, classes)) {
				List<MetaField> relationFields = this.buildAssociationFields(association, metaClass, classes);
				for (MetaField relationField : relationFields) {
					metaClass.addField(relationField);
				}
			}
		}

		return metaClass;
	}
	
	private boolean isAssociationOwnedByClass(Element association, MetaClass ownerClass, List<Element> classes) {
		List<Element> ownedEnds = this.directChildrenByName(association, "ownedEnd");
		for (Element ownedEnd : ownedEnds) {
			String ownerTypeId = this.attr(ownedEnd, "type");
			String ownerTypeName = this.resolveClassName(ownerTypeId, classes);
			if (ownerClass.getName().equals(ownerTypeName)) {
				return true;
			}
		}
		return false;
	}

	private MetaField buildAttribute(Element attribute, Map<String, String> dataTypes, List<Element> classes) {
		MetaField metaField = new MetaField();
		metaField.setId(this.attr(attribute, "xmi:id"));
		metaField.setName(this.attr(attribute, "name"));
		metaField.setOriginalType(this.attr(attribute, "type"));
		String resolvedType = this.resolveType(this.attr(attribute, "type"), dataTypes, classes);
		metaField.setType(resolvedType);
		metaField.setJavaType(this.resolveJavaType(resolvedType));
		metaField.setSqlType(this.resolveSqlType(resolvedType));
		metaField.setRelation(false);
		metaField.setCollection(this.isCollectionType(resolvedType));
		metaField.setJoinTableRequired(false);
		metaField.setRelationType(null);
		metaField.setForeignKeyColumn(null);

		Map<String, String> tags = this.extractTags(attribute);
		metaField.setTags(tags);
		metaField.setLabel(tags.get("label"));
		metaField.setWidget(tags.get("widget"));
		metaField.setJavaDoc(this.resolveDocumentation(tags, metaField.getName()));
		metaField.setSince(tags.get("since"));
		metaField.setRequired(this.isRequiredAttribute(tags));
		return metaField;
	}

	private List<MetaField> buildAssociationFields(Element association, MetaClass ownerClass, List<Element> classes) {
		List<MetaField> relationFields = new ArrayList<MetaField>();
		List<Element> ownedEnds = this.directChildrenByName(association, "ownedEnd");
		if (ownedEnds.size() < 2) {
			return relationFields;
		}

		for (int i = 0; i < ownedEnds.size(); i++) {
			Element ownerEnd = ownedEnds.get(i);
			String ownerTypeId = this.attr(ownerEnd, "type");
			String ownerTypeName = this.resolveClassName(ownerTypeId, classes);
			if (!ownerClass.getName().equals(ownerTypeName)) {
				continue;
			}

			for (int j = 0; j < ownedEnds.size(); j++) {
				if (i == j) {
					continue;
				}
				Element targetEnd = ownedEnds.get(j);
				String referencedId = this.attr(targetEnd, "type");
				String referencedType = this.resolveClassName(referencedId, classes);
				if (referencedType == null || ownerClass.getName().equals(referencedType)) {
					continue;
				}

				MetaField relationField = new MetaField();
				relationField.setId(this.attr(targetEnd, "xmi:id"));
				relationField.setName(this.resolveAssociationName(association, referencedType, relationFields.size()));
				relationField.setOriginalType(referencedId);
				relationField.setType(referencedType);
				relationField.setJavaType(referencedType);
				relationField.setSqlType(null);
				relationField.setRelation(true);
				relationField.setOwnerLowerBound(this.extractBound(ownerEnd, "lowerValue"));
				relationField.setOwnerUpperBound(this.extractBound(ownerEnd, "upperValue"));
				relationField.setTargetLowerBound(this.extractBound(targetEnd, "lowerValue"));
				relationField.setTargetUpperBound(this.extractBound(targetEnd, "upperValue"));
				relationField.setTags(this.extractTags(targetEnd));
				relationField.setJavaDoc(this.resolveDocumentation(relationField.getTags(), relationField.getName()));
				relationField.setSince(relationField.getTags().get("since"));
				relationField.setRelationType(this.resolveRelationType(relationField));
				relationField.setCollection(this.isCollectionMultiplicity(relationField.getTargetUpperBound()));
				relationField.setJoinTableRequired("MANY_TO_MANY".equals(relationField.getRelationType()));
				relationField.setForeignKeyColumn(this.resolveForeignKeyColumn(ownerClass.getName(), referencedType, relationField));
				relationField.setRequired(this.isRequiredMultiplicity(relationField.getTargetLowerBound()));
				relationFields.add(relationField);
			}
		}

		return relationFields;
	}

	private String resolveAssociationName(Element association, String referencedType, int index) {
		String associationName = this.attr(association, "name");
		if (associationName != null && !associationName.isEmpty()) {
			if (index == 0) {
				return associationName;
			}
			return associationName + index;
		}
		return this.lowerCaseFirst(referencedType);
	}

	private String resolveType(String rawType, Map<String, String> dataTypes, List<Element> classes) {
		if (rawType == null || rawType.isEmpty()) {
			return rawType;
		}
		if (dataTypes.containsKey(rawType)) {
			return dataTypes.get(rawType);
		}

		String className = this.resolveClassName(rawType, classes);
		if (className != null) {
			return className;
		}
		return rawType;
	}

	private String resolveJavaType(String resolvedType) {
		String normalizedType = this.normalizeTypeName(resolvedType);
		if ("string".equals(normalizedType)) {
			return "String";
		}
		if ("long".equals(normalizedType)) {
			return "Long";
		}
		if ("boolean".equals(normalizedType)) {
			return "Boolean";
		}
		if ("timestamp".equals(normalizedType)) {
			return "Timestamp";
		}
		if ("int".equals(normalizedType) || "integer".equals(normalizedType)) {
			return "Integer";
		}
		if ("list".equals(normalizedType)) {
			return "Object";
		}
		return resolvedType;
	}

	private String resolveSqlType(String resolvedType) {
		String normalizedType = this.normalizeTypeName(resolvedType);
		if ("string".equals(normalizedType)) {
			return "VARCHAR";
		}
		if ("long".equals(normalizedType)) {
			return "BIGINT";
		}
		if ("boolean".equals(normalizedType)) {
			return "BOOLEAN";
		}
		if ("timestamp".equals(normalizedType)) {
			return "TIMESTAMP";
		}
		if ("int".equals(normalizedType) || "integer".equals(normalizedType)) {
			return "INTEGER";
		}
		if ("list".equals(normalizedType)) {
			return null;
		}
		return null;
	}

	private boolean isCollectionType(String resolvedType) {
		return "list".equals(this.normalizeTypeName(resolvedType));
	}

	private boolean isRequiredAttribute(Map<String, String> tags) {
		String nullable = tags.get("nullable");
		if (nullable == null || nullable.isEmpty()) {
			return false;
		}
		return !"true".equalsIgnoreCase(nullable);
	}

	private String resolveRelationType(MetaField relationField) {
		boolean ownerMany = this.isCollectionMultiplicity(relationField.getOwnerUpperBound());
		boolean targetMany = this.isCollectionMultiplicity(relationField.getTargetUpperBound());

		if (ownerMany && targetMany) {
			return "MANY_TO_MANY";
		}
		if (!ownerMany && targetMany) {
			return "ONE_TO_MANY";
		}
		if (ownerMany) {
			return "MANY_TO_ONE";
		}
		return "ONE_TO_ONE";
	}

	private boolean isCollectionMultiplicity(String upperBound) {
		if (upperBound == null || upperBound.isEmpty()) {
			return false;
		}
		if ("*".equals(upperBound)) {
			return true;
		}
		try {
			return Integer.parseInt(upperBound) > 1;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private boolean isRequiredMultiplicity(String lowerBound) {
		if (lowerBound == null || lowerBound.isEmpty()) {
			return false;
		}
		try {
			return Integer.parseInt(lowerBound) > 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private String resolveForeignKeyColumn(String ownerClassName, String targetType, MetaField relationField) {
		if ("MANY_TO_MANY".equals(relationField.getRelationType())) {
			return null;
		}
		if ("ONE_TO_MANY".equals(relationField.getRelationType())) {
			return this.toSnakeCase(ownerClassName) + "_id";
		}
		return this.toSnakeCase(targetType) + "_id";
	}

	private String resolveClassName(String xmiId, List<Element> classes) {
		for (Element element : classes) {
			if (xmiId.equals(this.attr(element, "xmi:id"))) {
				return this.attr(element, "name");
			}
		}
		return null;
	}

	private Map<String, String> extractTags(Element element) {
		Map<String, String> tags = new LinkedHashMap<String, String>();
		List<Element> extensions = this.directChildrenByName(element, "xmi:Extension");

		for (Element extension : extensions) {
			List<Element> tagElements = this.directChildrenByName(extension, "tag");
			for (Element tagElement : tagElements) {
				NamedNodeMap attributes = tagElement.getAttributes();
				for (int i = 0; i < attributes.getLength(); i++) {
					Node attribute = attributes.item(i);
					tags.put(attribute.getNodeName(), this.cleanValue(attribute.getNodeValue()));
				}
			}
		}
		return tags;
	}

	private String extractBound(Element ownedEnd, String boundName) {
		List<Element> bounds = this.directChildrenByName(ownedEnd, boundName);
		if (bounds.isEmpty()) {
			return null;
		}
		return this.cleanValue(this.attr(bounds.get(0), "value"));
	}

	private List<Element> directChildrenByName(Element parent, String name) {
		List<Element> children = new ArrayList<Element>();
		Node child = parent.getFirstChild();
		while (child != null) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) child;
				if (name.equals(element.getNodeName())) {
					children.add(element);
				}
			}
			child = child.getNextSibling();
		}
		return children;
	}

	private List<Element> directChildren(Element parent) {
		List<Element> children = new ArrayList<Element>();
		Node child = parent.getFirstChild();
		while (child != null) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				children.add((Element) child);
			}
			child = child.getNextSibling();
		}
		return children;
	}

	private String attr(Element element, String name) {
		if (element.hasAttribute(name)) {
			return element.getAttribute(name);
		}
		return "";
	}

	private String cleanValue(String value) {
		if (value == null) {
			return null;
		}
		try {
			String decoded = URLDecoder.decode(value, "UTF-8");
			return decoded.replace("\r", "").replace("\n", "").trim();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 non supportato dalla JVM", e);
		}
	}

	private String normalizeTypeName(String resolvedType) {
		if (resolvedType == null) {
			return null;
		}
		return resolvedType.trim().toLowerCase();
	}

	private String resolveDocumentation(Map<String, String> tags, String fallbackName) {
		String description = tags.get("description");
		if (description != null && !description.trim().isEmpty()) {
			return description.trim();
		}

		String documentation = tags.get("documentation");
		if (documentation != null && !documentation.trim().isEmpty()) {
			return documentation.trim();
		}

		return "Elemento generato automaticamente: " + fallbackName;
	}

	private String lowerCaseFirst(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		return Character.toLowerCase(value.charAt(0)) + value.substring(1);
	}

	private String toSnakeCase(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char current = value.charAt(i);
			if (Character.isUpperCase(current) && i > 0) {
				builder.append('_');
			}
			builder.append(Character.toLowerCase(current));
		}
		return builder.toString();
	}
}
