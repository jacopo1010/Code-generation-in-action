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

<#function resolveTableName metaClass>
    <#if metaClass.table?? && metaClass.table?has_content>
        <#return metaClass.table>
    </#if>
    <#return toSqlName(metaClass.name)>
</#function>

<#function resolveColumnName field>
    <#if field.relation>
        <#if field.foreignKeyColumn?? && field.foreignKeyColumn?has_content>
            <#return field.foreignKeyColumn>
        </#if>
    </#if>
    <#return toSqlName(field.name)>
</#function>

<#function jdbcSetter javaType>
    <#switch javaType>
        <#case "String"><#return "setString">
        <#case "Long"><#return "setLong">
        <#case "Integer"><#return "setInt">
        <#case "Double"><#return "setDouble">
        <#case "Float"><#return "setFloat">
        <#case "Boolean"><#return "setBoolean">
        <#case "Timestamp"><#return "setTimestamp">
        <#case "Date"><#return "setDate">
        <#case "Time"><#return "setTime">
        <#case "BigDecimal"><#return "setBigDecimal">
        <#case "LocalDate"><#return "setObject">
        <#case "LocalDateTime"><#return "setObject">
        <#default><#return "setObject">
    </#switch>
</#function>

<#function resultSetGetter javaType>
    <#switch javaType>
        <#case "String"><#return "getString">
        <#case "Long"><#return "getLong">
        <#case "Integer"><#return "getInt">
        <#case "Double"><#return "getDouble">
        <#case "Float"><#return "getFloat">
        <#case "Boolean"><#return "getBoolean">
        <#case "Timestamp"><#return "getTimestamp">
        <#case "Date"><#return "getDate">
        <#case "Time"><#return "getTime">
        <#case "BigDecimal"><#return "getBigDecimal">
        <#case "LocalDate"><#return "getObject">
        <#case "LocalDateTime"><#return "getObject">
        <#default><#return "getObject">
    </#switch>
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

<#assign packageDao = packageDao!"jacopo.with.develop.dao">
<#assign modelPackage = modelPackage!(packageDao?replace(".dao", ".model"))>
<#assign entityName = metaClass.name>
<#assign tableName = resolveTableName(metaClass)>
<#assign allFields = metaClass.fields?values>
<#assign persistentFields = allFields?filter(field -> !field.collection && !field.relation)>
<#assign idFieldCandidates = persistentFields?filter(field -> field.name == "id" || resolveColumnName(field) == "id")>
<#assign idField = "">
<#if idFieldCandidates?size gt 0>
    <#assign idField = idFieldCandidates[0]>
</#if>
<#assign nonIdFields = persistentFields?filter(field -> !(field.name == "id" || resolveColumnName(field) == "id"))>
<#assign stringFields = nonIdFields?filter(field -> field.javaType == "String")>
<#assign manyToOneFields = allFields?filter(field -> field.relation && field.relationType == "MANY_TO_ONE" && field.foreignKeyColumn?? && field.foreignKeyColumn?has_content)>
<#assign usesTimestamp = persistentFields?filter(field -> field.javaType == "Timestamp")?size gt 0>
<#assign usesDate = persistentFields?filter(field -> field.javaType == "Date")?size gt 0>
<#assign usesTime = persistentFields?filter(field -> field.javaType == "Time")?size gt 0>
<#assign usesBigDecimal = persistentFields?filter(field -> field.javaType == "BigDecimal")?size gt 0>
<#assign usesLocalDate = persistentFields?filter(field -> field.javaType == "LocalDate")?size gt 0>
<#assign usesLocalDateTime = persistentFields?filter(field -> field.javaType == "LocalDateTime")?size gt 0>

package ${packageDao};

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
<#if usesTimestamp>
import java.sql.Timestamp;
</#if>
<#if usesDate>
import java.sql.Date;
</#if>
<#if usesTime>
import java.sql.Time;
</#if>
<#if usesBigDecimal>
import java.math.BigDecimal;
</#if>
<#if usesLocalDate>
import java.time.LocalDate;
</#if>
<#if usesLocalDateTime>
import java.time.LocalDateTime;
</#if>

import com.zaxxer.hikari.HikariDataSource;
import ${modelPackage}.${entityName};

public class ${entityName}Dao extends GenericDaoImpl<${entityName}, <#if idField?has_content>${idField.javaType}<#else>Long</#if>> {

    public ${entityName}Dao(HikariDataSource dataSource) {
        super(dataSource, ${entityName}.class);
    }

    @Override
    protected String getTableName() {
        return "${tableName}";
    }

    @Override
    protected String getIdColumn() {
        <#if idField?has_content>
        return "${resolveColumnName(idField)}";
        <#else>
        throw new UnsupportedOperationException("Nessun campo id configurato per ${entityName}");
        </#if>
    }

    @Override
    protected Set<String> getPersistentFields() {
        return Set.of(
<#list persistentFields as field>
            "${resolveColumnName(field)}"<#if field_has_next>,</#if>
</#list>
        );
    }

    @Override
    protected String getInsertSql() {
        <#if nonIdFields?size gt 0>
        return "INSERT INTO ${tableName} (<#list nonIdFields as field>${resolveColumnName(field)}<#if field_has_next>, </#if></#list>) VALUES (<#list nonIdFields as field>?<#if field_has_next>, </#if></#list>)";
        <#else>
        throw new UnsupportedOperationException("Nessun campo persistente disponibile per il salvataggio di ${entityName}");
        </#if>
    }

    @Override
    protected String getUpdateSql() {
        <#if idField?has_content && nonIdFields?size gt 0>
        return "UPDATE ${tableName} SET <#list nonIdFields as field>${resolveColumnName(field)} = ?<#if field_has_next>, </#if></#list> WHERE ${resolveColumnName(idField)} = ?";
        <#else>
        throw new UnsupportedOperationException("Update non supportato per ${entityName}");
        </#if>
    }

    @Override
    protected void bindInsertParameters(PreparedStatement statement, ${entityName} entity) throws SQLException {
        <#if nonIdFields?size gt 0>
<#list nonIdFields as field>
        statement.${jdbcSetter(field.javaType)}(${field_index + 1}, entity.get${field.name?cap_first}());
</#list>
        <#else>
        throw new UnsupportedOperationException("Nessun campo persistente disponibile per il salvataggio di ${entityName}");
        </#if>
    }

    @Override
    protected void bindUpdateParameters(PreparedStatement statement, ${entityName} entity) throws SQLException {
        <#if idField?has_content && nonIdFields?size gt 0>
<#list nonIdFields as field>
        statement.${jdbcSetter(field.javaType)}(${field_index + 1}, entity.get${field.name?cap_first}());
</#list>
        this.setIdParameter(statement, ${nonIdFields?size + 1}, entity.get${idField.name?cap_first}());
        <#else>
        throw new UnsupportedOperationException("Update non supportato per ${entityName}");
        </#if>
    }

    @Override
    protected void setIdParameter(PreparedStatement statement, int parameterIndex, <#if idField?has_content>${idField.javaType}<#else>Long</#if> id) throws SQLException {
        <#if idField?has_content>
        statement.${jdbcSetter(idField.javaType)}(parameterIndex, id);
        <#else>
        throw new UnsupportedOperationException("Nessun campo id configurato per ${entityName}");
        </#if>
    }

    @Override
    protected <#if idField?has_content>${idField.javaType}<#else>Long</#if> extractId(${entityName} entity) {
        <#if idField?has_content>
        return entity.get${idField.name?cap_first}();
        <#else>
        throw new UnsupportedOperationException("Nessun campo id configurato per ${entityName}");
        </#if>
    }

    @Override
    protected void assignGeneratedId(${entityName} entity, ResultSet generatedKeys) throws SQLException {
        <#if idField?has_content>
        if (generatedKeys.next()) {
            entity.set${idField.name?cap_first}(generatedKeys.${resultSetGetter(idField.javaType)}(1<#if resultSetGetter(idField.javaType) == "getObject" && idField.javaType != "String">, ${idField.javaType}.class</#if>));
        }
        </#if>
    }

    @Override
    protected ${entityName} mapRow(ResultSet resultSet) throws SQLException {
        ${entityName} entity = new ${entityName}();
<#list persistentFields as field>
        entity.set${field.name?cap_first}(resultSet.${resultSetGetter(field.javaType)}("${resolveColumnName(field)}"<#if resultSetGetter(field.javaType) == "getObject" && field.javaType != "String">, ${field.javaType}.class</#if>));
</#list>
        return entity;
    }

<#if stringFields?size gt 0>
    public List<${entityName}> searchByKeyword(String keyword) throws SQLException {
        List<${entityName}> results = new ArrayList<${entityName}>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return results;
        }

        String sql = "SELECT * FROM ${tableName} WHERE CONCAT(<#list stringFields as field>${resolveColumnName(field)}<#if field_has_next>, ' ', </#if></#list>) LIKE ?";

        try (java.sql.Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + keyword.trim() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(this.mapRow(resultSet));
                }
            }
        }

        return results;
    }
</#if>
<#list manyToOneFields as relationField>

    <#assign relationIdField = resolveRelationIdField(relationField)>
    <#assign relationIdType = "Long">
    <#if relationIdField?has_content>
        <#assign relationIdType = relationIdField.javaType>
    </#if>
    public List<${entityName}> findBy${relationField.name?cap_first}Id(${relationIdType} ${relationField.name}Id) throws SQLException {
        List<${entityName}> results = new ArrayList<${entityName}>();

        if (${relationField.name}Id == null) {
            return results;
        }

        String sql = "SELECT * FROM ${tableName} WHERE ${resolveColumnName(relationField)} = ?";

        try (java.sql.Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.${jdbcSetter(relationIdType)}(1, ${relationField.name}Id);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(this.mapRow(resultSet));
                }
            }
        }

        return results;
    }
</#list>
}
