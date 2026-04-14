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

<#assign packageDao = packageDao!((packageName!"jacopo.with.develop.model")?replace(".model", ".dao"))>
<#assign modelPackage = modelPackage!(packageName!"jacopo.with.develop.model")>
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
<#assign usesTimestamp = persistentFields?filter(field -> field.javaType == "Timestamp")?size gt 0>
<#assign usesDate = persistentFields?filter(field -> field.javaType == "Date")?size gt 0>
<#assign usesTime = persistentFields?filter(field -> field.javaType == "Time")?size gt 0>
<#assign usesBigDecimal = persistentFields?filter(field -> field.javaType == "BigDecimal")?size gt 0>
<#assign usesLocalDate = persistentFields?filter(field -> field.javaType == "LocalDate")?size gt 0>
<#assign usesLocalDateTime = persistentFields?filter(field -> field.javaType == "LocalDateTime")?size gt 0>

package ${packageDao};

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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

public class ${entityName}Dao {

    private HikariDataSource dataSource;

    public ${entityName}Dao(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

<#if idField?has_content>
    public ${entityName} findById(${idField.javaType} id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("L'id passato deve essere valorizzato");
        }

        String sql = "SELECT * FROM ${tableName} WHERE ${resolveColumnName(idField)} = ?";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.${jdbcSetter(idField.javaType)}(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                ${entityName} entity = null;

                if (resultSet.next()) {
                    entity = mapRow(resultSet);
                }

                return entity;
            }
        }
    }
</#if>

    public List<${entityName}> findAll() throws SQLException {
        String sql = "SELECT * FROM ${tableName}";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<${entityName}> results = new ArrayList<${entityName}>();

            while (resultSet.next()) {
                results.add(mapRow(resultSet));
            }

            return results;
        }
    }

    public ${entityName} save(${entityName} entity) throws SQLException {
        if (entity == null) {
            throw new IllegalArgumentException("L'entita passata deve essere valorizzata");
        }

<#if nonIdFields?size gt 0>
        String sql = "INSERT INTO ${tableName} (<#list nonIdFields as field>${resolveColumnName(field)}<#if field_has_next>, </#if></#list>) VALUES (<#list nonIdFields as field>?<#if field_has_next>, </#if></#list>)";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

<#list nonIdFields as field>
            statement.${jdbcSetter(field.javaType)}(${field_index + 1}, entity.get${field.name?cap_first}());
</#list>

            int righeModificate = statement.executeUpdate();

<#if idField?has_content>
            if (righeModificate > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.set${idField.name?cap_first}(generatedKeys.${resultSetGetter(idField.javaType)}(1<#if resultSetGetter(idField.javaType) == "getObject" && idField.javaType != "String">, ${idField.javaType}.class</#if>));
                    }
                }
            }
</#if>
            return entity;
        }
<#else>
        throw new UnsupportedOperationException("Nessun campo persistente disponibile per il salvataggio di ${entityName}");
</#if>
    }

<#if idField?has_content && nonIdFields?size gt 0>
    public boolean update(${entityName} entity) throws SQLException {
        if (entity == null) {
            throw new IllegalArgumentException("L'entita passata deve essere valorizzata");
        }
        if (entity.get${idField.name?cap_first}() == null) {
            throw new IllegalArgumentException("L'id dell'entita deve essere valorizzato");
        }

        String sql = "UPDATE ${tableName} SET <#list nonIdFields as field>${resolveColumnName(field)} = ?<#if field_has_next>, </#if></#list> WHERE ${resolveColumnName(idField)} = ?";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

<#list nonIdFields as field>
            statement.${jdbcSetter(field.javaType)}(${field_index + 1}, entity.get${field.name?cap_first}());
</#list>
            statement.${jdbcSetter(idField.javaType)}(${nonIdFields?size + 1}, entity.get${idField.name?cap_first}());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(${idField.javaType} id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("L'id passato deve essere valorizzato");
        }

        String sql = "DELETE FROM ${tableName} WHERE ${resolveColumnName(idField)} = ?";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.${jdbcSetter(idField.javaType)}(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean existsById(${idField.javaType} id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("L'id passato deve essere valorizzato");
        }

        String sql = "SELECT 1 FROM ${tableName} WHERE ${resolveColumnName(idField)} = ?";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.${jdbcSetter(idField.javaType)}(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
</#if>

    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM ${tableName}";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();
        }
    }

    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM ${tableName}";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            long total = 0L;

            if (resultSet.next()) {
                total = resultSet.getLong(1);
            }

            return total;
        }
    }

<#if stringFields?size gt 0>
    public List<${entityName}> searchByKeyword(String keyword) throws SQLException {
        List<${entityName}> results = new ArrayList<${entityName}>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return results;
        }

        String sql = "SELECT * FROM ${tableName} WHERE CONCAT(<#list stringFields as field>${resolveColumnName(field)}<#if field_has_next>, ' ', </#if></#list>) LIKE ?";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + keyword.trim() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapRow(resultSet));
                }
            }
        }

        return results;
    }
</#if>

    public List<${entityName}> searchByField(String field, Object value) throws SQLException {
        if (field == null || field.isBlank() || value == null) {
            throw new IllegalArgumentException("Il campo e il valore devono essere valorizzati");
        }

        Set<String> allowedFields = Set.of(
<#list persistentFields as field>
            "${resolveColumnName(field)}"<#if field_has_next>,</#if>
</#list>
        );

        if (!allowedFields.contains(field)) {
            throw new IllegalArgumentException("Campo non consentito: " + field);
        }

        String sql = "SELECT * FROM ${tableName} WHERE " + field + " = ?";

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, value);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<${entityName}> results = new ArrayList<${entityName}>();

                while (resultSet.next()) {
                    results.add(mapRow(resultSet));
                }

                return results;
            }
        }
    }

    private ${entityName} mapRow(ResultSet resultSet) throws SQLException {
        ${entityName} entity = new ${entityName}();
<#list persistentFields as field>
        entity.set${field.name?cap_first}(resultSet.${resultSetGetter(field.javaType)}("${resolveColumnName(field)}"<#if resultSetGetter(field.javaType) == "getObject" && field.javaType != "String">, ${field.javaType}.class</#if>));
</#list>
        return entity;
    }
}
