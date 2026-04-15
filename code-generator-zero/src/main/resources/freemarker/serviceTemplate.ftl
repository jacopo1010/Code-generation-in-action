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
<#assign packageModel = packageModel!(packageDao?replace(".dao", ".model"))>
<#assign packageDao = packageDao!"jacopo.with.develop.dao">
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

package ${packageService};

import java.sql.SQLException;
import java.util.List;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ${packageModel}.${entityName};
import ${packageDao}.${entityName}Dao;
import ${packageDao}.GenericDao;

public class ${entityName}Service {

    private final HikariDataSource dataSource;
    private final GenericDao<${entityName}, <#if idField?has_content>${idField.javaType}<#else>Long</#if>> dao;

    public ${entityName}Service() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://localhost:3306/${entityName?lower_case}");
        hikariConfig.setUsername("root");
        hikariConfig.setPassword("root");
        hikariConfig.setMaximumPoolSize(10);
        this.dataSource = new HikariDataSource(hikariConfig);
        this.dao = new ${entityName}Dao(this.dataSource);
    }

<#if idField?has_content>
    public ${entityName} findById(${idField.javaType} id) throws SQLException {
        return this.dao.findById(id);
    }

    public boolean delete(${idField.javaType} id) throws SQLException {
        return this.dao.delete(id);
    }

    public boolean existsById(${idField.javaType} id) throws SQLException {
        return this.dao.existsById(id);
    }
</#if>

    public List<${entityName}> findAll() throws SQLException {
        return this.dao.findAll();
    }

    public ${entityName} save(${entityName} entity) throws SQLException {
        return this.dao.save(entity);
    }

    public boolean update(${entityName} entity) throws SQLException {
        return this.dao.update(entity);
    }

    public void deleteAll() throws SQLException {
        this.dao.deleteAll();
    }

    public long count() throws SQLException {
        return this.dao.count();
    }

    public List<${entityName}> searchByField(String field, Object value) throws SQLException {
        return this.dao.searchByField(field, value);
    }

<#if stringFields?size gt 0>
    public List<${entityName}> findByKeyword(String keyword) throws SQLException {
        return this.getDao().searchByKeyword(keyword);
    }

</#if>
<#list manyToOneFields as relationField>
    <#assign relationIdField = resolveRelationIdField(relationField)>
    <#assign relationIdType = "Long">
    <#if relationIdField?has_content>
        <#assign relationIdType = relationIdField.javaType>
    </#if>
    public List<${entityName}> findBy${relationField.name?cap_first}Id(${relationIdType} ${relationField.name}Id) throws SQLException {
        return this.getDao().findBy${relationField.name?cap_first}Id(${relationField.name}Id);
    }

</#list>
    public ${entityName}Dao getDao() {
        return (${entityName}Dao) this.dao;
    }
}
