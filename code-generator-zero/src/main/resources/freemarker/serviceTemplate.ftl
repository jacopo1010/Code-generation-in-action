<#ftl output_format="plainText">
<#function toSqlName value>
    <#if !value?? || !value?has_content>
        <#return "">
    </#if>
    <#return value
        ?replace("([a-z0-9])([A-Z])", "$1_$2", "r")
        ?replace("[^A-Za-z0-9_]", "_", "r")
        ?lower_case>
</#function>

<#function resolveColumnName field>
    <#if field.relation>
        <#if field.foreignKeyColumn?? && field.foreignKeyColumn?has_content>
            <#return field.foreignKeyColumn>
        </#if>
    </#if>
    <#return toSqlName(field.name)>
</#function>

<#function resolveRelationIdField field>
    <#if metaClasses?? && metaClasses[field.javaType]??>
        <#assign targetMetaClass = metaClasses[field.javaType]>
        <#assign targetFields = targetMetaClass.fields?values>
        <#assign targetPersistentFields = targetFields?filter(candidate -> !candidate.collection && !candidate.relation)>
        <#assign targetIdFields = targetPersistentFields?filter(candidate -> candidate.name == "id" || resolveColumnName(candidate) == "id")>
        <#if targetIdFields?size gt 0>
            <#return targetIdFields[0]>
        </#if>
    </#if>
    <#return "">
</#function>

<#assign entityName = metaClass.name>
<#assign packageModel = packageModel!"jacopo.with.develop.model">
<#assign packageRepository = packageRepository!"jacopo.with.develop.repository">
<#assign packageService = packageService!"jacopo.with.develop.service">
<#assign allFields = metaClass.fields?values>
<#assign persistentFields = allFields?filter(field -> !field.collection && !field.relation)>
<#assign idFieldCandidates = persistentFields?filter(field -> field.name == "id" || resolveColumnName(field) == "id")>
<#assign idField = "">
<#if idFieldCandidates?size gt 0>
    <#assign idField = idFieldCandidates[0]>
</#if>
<#assign stringFields = persistentFields?filter(field -> field.javaType == "String")>
<#assign manyToOneFields = allFields?filter(field -> field.relation && field.relationType == "MANY_TO_ONE" && field.foreignKeyColumn?? && field.foreignKeyColumn?has_content)>
<#assign relationTypesToImport = []>
<#list manyToOneFields as field>
    <#if !relationTypesToImport?seq_contains(field.javaType)>
        <#assign relationTypesToImport = relationTypesToImport + [field.javaType]>
    </#if>
</#list>
<#assign creationTimestampField = "">
<#assign lastUpdateTimestampField = "">
<#list persistentFields as field>
    <#if field.javaType == "Timestamp" && field.name == "creationTimeStamp">
        <#assign creationTimestampField = field>
    </#if>
    <#if field.javaType == "Timestamp" && field.name == "lastUpdateTimeStamp">
        <#assign lastUpdateTimestampField = field>
    </#if>
</#list>
<#assign hasTechnicalTimestampFields = creationTimestampField?has_content && lastUpdateTimestampField?has_content>

package ${packageService};

import java.util.List;
import java.util.Optional;
<#if hasTechnicalTimestampFields>
import java.sql.Timestamp;
</#if>

import ${packageModel}.${entityName};
<#list relationTypesToImport as relationType>
import ${packageModel}.${relationType};
</#list>
import ${packageRepository}.${entityName}Repository;
<#list relationTypesToImport as relationType>
import ${packageRepository}.${relationType}Repository;
</#list>

public class ${entityName}ServiceBase {

    protected final ${entityName}Repository repository;
<#list manyToOneFields as relationField>
    protected final ${relationField.javaType}Repository ${relationField.javaType?uncap_first}Repository;
</#list>

    protected ${entityName}ServiceBase(${entityName}Repository repository<#list manyToOneFields as relationField>, ${relationField.javaType}Repository ${relationField.javaType?uncap_first}Repository</#list>) {
        this.repository = repository;
<#list manyToOneFields as relationField>
        this.${relationField.javaType?uncap_first}Repository = ${relationField.javaType?uncap_first}Repository;
</#list>
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    public List<${entityName}> findAll() {
        return this.repository.findAll();
    }

<#if idField?has_content>
    public Optional<${entityName}> findById(${idField.javaType} id) {
        return this.repository.findById(id);
    }

    public boolean existsById(${idField.javaType} id) {
        return this.repository.existsById(id);
    }
</#if>

    public long count() {
        return this.repository.count();
    }

<#if stringFields?size gt 0>
    public List<${entityName}> findByKeyword(String keyword) {
        return this.repository.findByKeyword(keyword);
    }

</#if>
<#list manyToOneFields as relationField>
    <#assign relationIdField = resolveRelationIdField(relationField)>
    <#assign relationIdType = relationIdField?has_content?then(relationIdField.javaType, "Long")>
    public List<${entityName}> findBy${relationField.name?cap_first}Id(${relationIdType} id) {
        return this.repository.findBy${relationField.name?cap_first}Id(id);
    }

</#list>

    // -------------------------------------------------------------------------
    // WRITE
    // -------------------------------------------------------------------------

    public ${entityName} save(${entityName} entity) {
        this.prepareForCreate(entity);
        return this.repository.save(entity);
    }

    public boolean update(${entityName} entity) {
        this.prepareForUpdate(entity);
        return this.repository.update(entity);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

<#if idField?has_content>
    public boolean delete(${idField.javaType} id) {
        return this.repository.delete(id);
    }

</#if>
    public void deleteAll() {
        this.repository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // VALIDAZIONE
    // -------------------------------------------------------------------------

    private void prepareForCreate(${entityName} entity) {
        this.validateEntityForWrite(entity);
<#if hasTechnicalTimestampFields>
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (entity.get${creationTimestampField.name?cap_first}() == null) {
            entity.set${creationTimestampField.name?cap_first}(now);
        }
        entity.set${lastUpdateTimestampField.name?cap_first}(now);
</#if>
    }

    private void prepareForUpdate(${entityName} entity) {
        this.validateEntityForWrite(entity);
<#if idField?has_content>
        if (entity.get${idField.name?cap_first}() == null) {
            throw new IllegalArgumentException("Id obbligatorio per l'aggiornamento di ${entityName}");
        }
<#else>
        throw new UnsupportedOperationException("Update non supportato: nessun campo id in ${entityName}");
</#if>
<#if hasTechnicalTimestampFields>
        if (entity.get${creationTimestampField.name?cap_first}() == null) {
            entity.set${creationTimestampField.name?cap_first}(new Timestamp(System.currentTimeMillis()));
        }
        entity.set${lastUpdateTimestampField.name?cap_first}(new Timestamp(System.currentTimeMillis()));
</#if>
    }

    private void validateEntityForWrite(${entityName} entity) {
        if (entity == null) {
            throw new IllegalArgumentException("${entityName} obbligatorio");
        }
<#list manyToOneFields as relationField>
    <#assign relIdField = resolveRelationIdField(relationField)>
    <#assign relIdGetter = relIdField?has_content?then("get" + relIdField.name?cap_first + "()", "getId()")>

        ${relationField.javaType} ${relationField.name} = entity.get${relationField.name?cap_first}();
<#if relationField.required>
        if (${relationField.name} == null || ${relationField.name}.${relIdGetter} == null) {
            throw new IllegalArgumentException("${relationField.javaType} associato obbligatorio per ${entityName}");
        }
</#if>
        if (${relationField.name} != null) {
            if (${relationField.name}.${relIdGetter} == null) {
                throw new IllegalArgumentException("Id di ${relationField.javaType} associato obbligatorio");
            }
            if (!this.${relationField.javaType?uncap_first}Repository.existsById(${relationField.name}.${relIdGetter})) {
                throw new IllegalArgumentException("${relationField.javaType} associato non esistente: "
                    + ${relationField.name}.${relIdGetter});
            }
        }
</#list>
    }
}
