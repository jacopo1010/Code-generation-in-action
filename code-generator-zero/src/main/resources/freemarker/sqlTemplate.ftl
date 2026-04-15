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

<#function resolveReferenceTableName field>
    <#if metaClasses[field.type]??>
        <#return resolveTableName(metaClasses[field.type])>
    </#if>
    <#return toSqlName(field.type)>
</#function>

<#function resolveColumnName field>
    <#if field.relation>
        <#if field.foreignKeyColumn?? && field.foreignKeyColumn?has_content>
            <#return field.foreignKeyColumn>
        </#if>
    </#if>
    <#return toSqlName(field.name)>
</#function>

<#function resolveSqlType field>
    <#if field.sqlType?? && field.sqlType?has_content>
        <#if field.sqlType == "VARCHAR">
            <#return "VARCHAR(255)">
        </#if>
        <#return field.sqlType>
    </#if>
    <#return "VARCHAR(255)">
</#function>

<#function resolveColumnDefinition field>
    <#assign sqlType = resolveSqlType(field)>
    <#if resolveColumnName(field) == "id" && sqlType == "BIGINT">
        <#return sqlType + " AUTO_INCREMENT">
    </#if>
    <#return sqlType>
</#function>

<#assign emittedJoinTables = []>
<#list metaClasses?values as metaClass>
<#assign tableName = resolveTableName(metaClass)>
<#assign allFields = metaClass.fields?values>
<#assign simpleFields = allFields?filter(field -> !field.relation)>
<#assign foreignKeyFields = allFields?filter(field -> field.relation && !field.collection && !field.joinTableRequired && field.foreignKeyColumn?? && field.foreignKeyColumn?has_content)>
<#assign primaryKeyFields = simpleFields?filter(field -> resolveColumnName(field) == "id")>
<#assign uniqueFields = simpleFields?filter(field -> field.tags["unique"]?? && field.tags["unique"]?lower_case == "true")>
<#assign hasPrimaryKey = primaryKeyFields?size gt 0>

CREATE TABLE IF NOT EXISTS ${tableName} (
<#list simpleFields as field>
    ${resolveColumnName(field)} ${resolveColumnDefinition(field)}<#if field.required> NOT NULL</#if><#if field_has_next || foreignKeyFields?size gt 0 || hasPrimaryKey>,</#if>
</#list>
<#list foreignKeyFields as field>
    ${resolveColumnName(field)} BIGINT<#if field.required> NOT NULL</#if><#if field_has_next || hasPrimaryKey>,</#if>
</#list>
<#if hasPrimaryKey>
    PRIMARY KEY (${resolveColumnName(primaryKeyFields[0])})
</#if>
);

<#list foreignKeyFields as field>
ALTER TABLE ${tableName}
    ADD CONSTRAINT fk_${tableName}_${resolveColumnName(field)}
    FOREIGN KEY (${resolveColumnName(field)}) REFERENCES ${resolveReferenceTableName(field)}(id);

</#list>
<#list uniqueFields as field>
ALTER TABLE ${tableName}
    ADD CONSTRAINT uq_${tableName}_${resolveColumnName(field)}
    UNIQUE (${resolveColumnName(field)});

</#list>
<#assign manyToManyFields = allFields?filter(field -> field.joinTableRequired)>
<#list manyToManyFields as field>
    <#assign referenceTable = resolveReferenceTableName(field)>
    <#assign joinTables = [tableName, referenceTable]?sort>
    <#assign joinTableName = joinTables[0] + "_" + joinTables[1]>
    <#if !emittedJoinTables?seq_contains(joinTableName)>
        <#assign emittedJoinTables = emittedJoinTables + [joinTableName]>
        <#assign leftColumn = joinTables[0] + "_id">
        <#assign rightColumn = joinTables[1] + "_id">
CREATE TABLE IF NOT EXISTS ${joinTableName} (
    ${leftColumn} BIGINT NOT NULL,
    ${rightColumn} BIGINT NOT NULL,
    PRIMARY KEY (${leftColumn}, ${rightColumn}),
    FOREIGN KEY (${leftColumn}) REFERENCES ${joinTables[0]}(id),
    FOREIGN KEY (${rightColumn}) REFERENCES ${joinTables[1]}(id)
);

    </#if>
</#list>
</#list>
