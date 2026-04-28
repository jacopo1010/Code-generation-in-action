<#ftl output_format="plainText">
<#function toEntityAlias value>
    <#if !value?? || !value?has_content><#return "o"></#if>
    <#return value?substring(0, 1)?lower_case>
</#function>
<#function resolveRelationIdField field>
    <#if metaClasses?? && metaClasses[field.javaType]??>
        <#assign targetFields = metaClasses[field.javaType].fields?values>
        <#assign targetIdFields = targetFields?filter(f -> !f.collection && !f.relation && f.name == "id")>
        <#if targetIdFields?size gt 0><#return targetIdFields[0]></#if>
    </#if>
    <#return "">
</#function>

<#assign entityName = metaClass.name>
<#assign entityAlias = toEntityAlias(entityName)>
<#assign allFields = metaClass.fields?values>
<#assign persistentFields = allFields?filter(f -> !f.collection && !f.relation)>
<#assign stringFields = persistentFields?filter(f -> f.javaType == "String")>
<#assign manyToOneFields = allFields?filter(f -> f.relation && f.relationType == "MANY_TO_ONE")>
<#assign repositoryConfig = jakartaEe.repository>

package ${packageRepository};

import java.util.Collections;
import java.util.List;

<#list repositoryConfig.imports as importLine>
import ${importLine};
</#list>
import jakarta.persistence.TypedQuery;

import ${packageModel}.${entityName};

<#list repositoryConfig.classAnnotations as annotation>
${annotation}
</#list>
public class ${entityName}Repository extends SimpleRepositoryImpl<${entityName}> {

    public ${entityName}Repository() {
        super(${entityName}.class);
    }

<#if stringFields?size gt 0>
    public List<${entityName}> findByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        TypedQuery<${entityName}> query = this.getEntityManager().createQuery(
                "SELECT ${entityAlias} FROM ${entityName} ${entityAlias} WHERE "
<#list stringFields as field>
                + "LOWER(${entityAlias}.${field.name}) LIKE :keyword"<#if field_has_next>
                + " OR "
</#if></#list>
                ,
                ${entityName}.class);
        query.setParameter("keyword", pattern);
        return query.getResultList();
    }

</#if>
<#list stringFields as field>
    public List<${entityName}> findBy${field.name?cap_first}(String ${field.name}) {
        TypedQuery<${entityName}> query = this.getEntityManager().createQuery(
                "SELECT ${entityAlias} FROM ${entityName} ${entityAlias} WHERE ${entityAlias}.${field.name} = :${field.name}",
                ${entityName}.class);
        query.setParameter("${field.name}", ${field.name});
        return query.getResultList();
    }

</#list>
<#list manyToOneFields as relField>
    <#assign relIdField = resolveRelationIdField(relField)>
    <#assign relIdType = relIdField?has_content?then(relIdField.javaType, "Long")>
    public List<${entityName}> findBy${relField.name?cap_first}Id(${relIdType} id) {
        TypedQuery<${entityName}> query = this.getEntityManager().createQuery(
                "SELECT ${entityAlias} FROM ${entityName} ${entityAlias} WHERE ${entityAlias}.${relField.name}.id = :id",
                ${entityName}.class);
        query.setParameter("id", id);
        return query.getResultList();
    }

</#list>
}
