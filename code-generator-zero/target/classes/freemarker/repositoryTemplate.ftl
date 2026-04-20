<#ftl output_format="plainText">
<#function toSqlName value>
    <#if !value?? || !value?has_content><#return ""></#if>
    <#return value
        ?replace("([a-z0-9])([A-Z])", "$1_$2", "r")
        ?replace("[^A-Za-z0-9_]", "_", "r")
        ?lower_case>
</#function>
<#function resolveTableName metaClass>
    <#if metaClass.table?? && metaClass.table?has_content>
        <#return metaClass.table>
    </#if>
    <#return toSqlName(metaClass.name)>
</#function>
<#function resolveColumnName field>
    <#if field.relation && field.foreignKeyColumn?? && field.foreignKeyColumn?has_content>
        <#return field.foreignKeyColumn>
    </#if>
    <#return toSqlName(field.name)>
</#function>
<#function toJooqConstant name>
    <#return name
        ?replace("([a-z0-9])([A-Z])", "$1_$2", "r")
        ?upper_case>
</#function>
<#function toJavaName value>
    <#if !value?? || !value?has_content><#return ""></#if>
    <#assign normalized = value?replace("[^A-Za-z0-9]+", " ", "r")>
    <#assign parts = normalized?trim?split(" ")>
    <#assign javaName = "">
    <#list parts as part>
        <#if part?has_content>
            <#assign javaName = javaName + part?lower_case?cap_first>
        </#if>
    </#list>
    <#return javaName>
</#function>
<#function resolveRelationIdField field>
    <#if metaClasses?? && metaClasses[field.javaType]??>
        <#assign targetFields = metaClasses[field.javaType].fields?values>
        <#assign targetIdFields = targetFields?filter(f -> !f.collection && !f.relation && (f.name == "id" || resolveColumnName(f) == "id"))>
        <#if targetIdFields?size gt 0><#return targetIdFields[0]></#if>
    </#if>
    <#return "">
</#function>

<#assign entityName = metaClass.name>
<#assign tableName = resolveTableName(metaClass)>
<#assign tableConstant = toJooqConstant(tableName)>
<#assign tableClassName = toJavaName(tableName)>
<#assign allFields = metaClass.fields?values>
<#assign persistentFields = allFields?filter(f -> !f.collection && !f.relation)>
<#assign relationFields = allFields?filter(f -> f.relation && !f.collection && !f.joinTableRequired && f.foreignKeyColumn?? && f.foreignKeyColumn?has_content)>
<#assign idFieldCandidates = persistentFields?filter(f -> f.name == "id" || resolveColumnName(f) == "id")>
<#assign idField = "">
<#if idFieldCandidates?size gt 0><#assign idField = idFieldCandidates[0]></#if>
<#assign stringFields = persistentFields?filter(f -> f.javaType == "String")>
<#assign manyToOneFields = allFields?filter(f -> f.relation && f.relationType == "MANY_TO_ONE" && f.foreignKeyColumn?? && f.foreignKeyColumn?has_content)>
<#assign relationTypesToImport = []>
<#list relationFields as field>
    <#if !relationTypesToImport?seq_contains(field.javaType)>
        <#assign relationTypesToImport = relationTypesToImport + [field.javaType]>
    </#if>
</#list>
<#assign usesTimestamp = persistentFields?filter(f -> f.javaType == "Timestamp")?size gt 0>

package ${packageRepository};

<#if usesTimestamp>
import java.time.LocalDateTime;
import java.sql.Timestamp;
</#if>
import java.util.List;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import com.zaxxer.hikari.HikariDataSource;

import ${packageModel}.${entityName};
<#list relationTypesToImport as relationType>
import ${packageModel}.${relationType};
</#list>
import static ${jooqPackage}.tables.${tableClassName}.${tableConstant};

public class ${entityName}Repository extends SimpleRepositoryImpl<${entityName}> {

    public ${entityName}Repository(HikariDataSource dataSource) {
        super(dataSource);
    }

<#if usesTimestamp>
    private Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

</#if>
    @Override
    protected ${entityName} toModel(Record record) {
        ${entityName} entity = new ${entityName}();
<#list persistentFields as field>
    <#if field.javaType == "Timestamp">
        entity.set${field.name?cap_first}(this.toTimestamp(record.get(${tableConstant}.${toJooqConstant(resolveColumnName(field))}, LocalDateTime.class)));
    <#else>
        entity.set${field.name?cap_first}(record.get(${tableConstant}.${toJooqConstant(resolveColumnName(field))}, ${field.javaType}.class));
    </#if>
</#list>
<#list relationFields as field>
        <#assign relIdField = resolveRelationIdField(field)>
        <#if relIdField?has_content>
        ${field.javaType} ${field.name} = new ${field.javaType}();
        ${field.name}.set${relIdField.name?cap_first}(record.get(${tableConstant}.${toJooqConstant(resolveColumnName(field))}, ${relIdField.javaType}.class));
        entity.set${field.name?cap_first}(${field.name});
        </#if>
</#list>
        return entity;
    }

    @Override
    protected Table<?> getTable() {
        return ${tableConstant};
    }

    @Override
    protected void bindRecord(UpdatableRecord<?> record, ${entityName} entity) {
<#list persistentFields as field>
    <#if !(field.name == "id" || resolveColumnName(field) == "id")>
        <#if field.javaType == "Timestamp">
        record.set(${tableConstant}.${toJooqConstant(resolveColumnName(field))}, this.toLocalDateTime(entity.get${field.name?cap_first}()));
        <#else>
        record.set(${tableConstant}.${toJooqConstant(resolveColumnName(field))}, entity.get${field.name?cap_first}());
        </#if>
    </#if>
</#list>
<#list relationFields as field>
        <#assign relIdField = resolveRelationIdField(field)>
        <#if relIdField?has_content>
        if (entity.get${field.name?cap_first}() != null) {
            record.set(${tableConstant}.${toJooqConstant(resolveColumnName(field))}, entity.get${field.name?cap_first}().get${relIdField.name?cap_first}());
        }
        </#if>
</#list>
    }

<#if idField?has_content>
    @Override
    protected void assignId(${entityName} entity, Long id) {
        entity.set${idField.name?cap_first}(id);
    }

    @Override
    protected Field<Long> getIdField() {
        return ${tableConstant}.${toJooqConstant(resolveColumnName(idField))};
    }

    public boolean update(${entityName} entity) {
        int updated = this.getDsl()
            .update(${tableConstant})
<#list persistentFields as field>
    <#if !(field.name == "id" || resolveColumnName(field) == "id")>
        <#if field.javaType == "Timestamp">
            .set(${tableConstant}.${toJooqConstant(resolveColumnName(field))}, this.toLocalDateTime(entity.get${field.name?cap_first}()))
        <#else>
            .set(${tableConstant}.${toJooqConstant(resolveColumnName(field))}, entity.get${field.name?cap_first}())
        </#if>
    </#if>
</#list>
<#list relationFields as field>
        <#assign relIdField = resolveRelationIdField(field)>
        <#if relIdField?has_content>
            .set(${tableConstant}.${toJooqConstant(resolveColumnName(field))},
                entity.get${field.name?cap_first}() != null ? entity.get${field.name?cap_first}().get${relIdField.name?cap_first}() : null)
        </#if>
</#list>
            .where(${tableConstant}.${toJooqConstant(resolveColumnName(idField))}.eq(entity.get${idField.name?cap_first}()))
            .execute();
        return updated > 0;
    }
<#else>
    @Override
    protected void assignId(${entityName} entity, Long id) {
        throw new UnsupportedOperationException("Assegnazione id non supportata: nessun campo id in ${entityName}");
    }

    @Override
    protected Field<Long> getIdField() {
        throw new UnsupportedOperationException("Campo id non supportato in ${entityName}");
    }

    public boolean update(${entityName} entity) {
        throw new UnsupportedOperationException("Update non supportato: nessun campo id in ${entityName}");
    }
</#if>

<#if stringFields?size gt 0>
    public List<${entityName}> findByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        String pattern = "%" + keyword.trim() + "%";
        return this.getDsl()
            .selectFrom(${tableConstant})
            .where(
<#list stringFields as field>
                ${tableConstant}.${toJooqConstant(resolveColumnName(field))}.likeIgnoreCase(pattern)<#if field_has_next>
                .or(</#if></#list><#list stringFields as field><#if field_has_next>)</#if></#list>

            )
            .fetch()
            .map(this::toModel);
    }
</#if>

<#list manyToOneFields as relField>
    <#assign relIdField = resolveRelationIdField(relField)>
    <#assign relIdType = relIdField?has_content?then(relIdField.javaType, "Long")>
    public List<${entityName}> findBy${relField.name?cap_first}Id(${relIdType} id) {
        return this.getDsl()
            .selectFrom(${tableConstant})
            .where(${tableConstant}.${toJooqConstant(resolveColumnName(relField))}.eq(id))
            .fetch()
            .map(this::toModel);
    }

</#list>
}
