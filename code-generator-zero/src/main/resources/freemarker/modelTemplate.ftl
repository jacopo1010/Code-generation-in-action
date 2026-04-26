package ${packageName!"jacopo.with.develop.model"};

<#assign fields = metaClass.fields?values>
<#assign collectionFields = fields?filter(field -> field.collection)>
<#assign relationFields = fields?filter(field -> field.relation)>
<#assign usesTimestamp = fields?filter(field -> field.javaType == "Timestamp")?size gt 0>
<#assign usesJpa = true>
<#assign usesHibernateOnDelete = relationFields?filter(field -> field.cascadeOnDelete?? && field.cascadeOnDelete == "CASCADE")?size gt 0>
<#assign usesHibernateFetch = relationFields?size gt 0>
<#function toSqlName value>
    <#if !value?? || !value?has_content>
        <#return "">
    </#if>
    <#return value
        ?replace("([a-z0-9])([A-Z])", "$1_$2", "r")
        ?replace("[^A-Za-z0-9_]", "_", "r")
        ?lower_case>
</#function>
<#function resolveTableName currentMetaClass>
    <#if currentMetaClass.table?? && currentMetaClass.table?has_content>
        <#return currentMetaClass.table>
    </#if>
    <#return toSqlName(currentMetaClass.name)>
</#function>
<#function resolveColumnName field>
    <#if field.tags["columnName"]?? && field.tags["columnName"]?has_content>
        <#return field.tags["columnName"]>
    </#if>
    <#if field.relation && field.foreignKeyColumn?? && field.foreignKeyColumn?has_content>
        <#return field.foreignKeyColumn>
    </#if>
    <#return toSqlName(field.name)>
</#function>
<#function resolveReferenceTableName field>
    <#if metaClasses[field.type]??>
        <#return resolveTableName(metaClasses[field.type])>
    </#if>
    <#return toSqlName(field.type)>
</#function>
<#function resolveMappedBy field>
    <#if field.tags["mappedBy"]?? && field.tags["mappedBy"]?has_content>
        <#return field.tags["mappedBy"]>
    </#if>
    <#return field.name>
</#function>
<#function resolveJoinTableName field>
    <#if field.tags["joinTableName"]?? && field.tags["joinTableName"]?has_content>
        <#return field.tags["joinTableName"]>
    </#if>
    <#assign joinTables = [resolveTableName(metaClass), resolveReferenceTableName(field)]?sort>
    <#return joinTables[0] + "_" + joinTables[1]>
</#function>
<#function resolveJoinColumnName field>
    <#return resolveTableName(metaClass) + "_id">
</#function>
<#function resolveInverseJoinColumnName field>
    <#return resolveReferenceTableName(field) + "_id">
</#function>
<#function isIdField field>
    <#return !field.relation && resolveColumnName(field) == "id">
</#function>
<#function isOwningManyToMany field>
    <#if field.tags["mappedBy"]?? && field.tags["mappedBy"]?has_content>
        <#return false>
    </#if>
    <#assign ownerTable = resolveTableName(metaClass)>
    <#assign referenceTable = resolveReferenceTableName(field)>
    <#assign joinTables = [ownerTable, referenceTable]?sort>
    <#return joinTables[0] == ownerTable>
</#function>
<#function hasJpaCascade field>
    <#return field.tags["cascade"]?? && field.tags["cascade"]?has_content>
</#function>
<#function resolveJpaCascade field>
    <#assign rawCascade = field.tags["cascade"]!"">
    <#assign normalized = rawCascade?upper_case?replace("\\s+", "", "r")>
    <#assign values = normalized?split(",")>
    <#assign output = []>
    <#list values as value>
        <#if value?has_content>
            <#assign output = output + ["CascadeType." + value]>
        </#if>
    </#list>
    <#return output?join(", ")>
</#function>
<#if usesTimestamp>
import java.sql.Timestamp;
</#if>
<#if collectionFields?size gt 0>
import java.util.List;
import java.util.ArrayList;
</#if>
<#if usesJpa>
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
</#if>
<#if usesHibernateFetch>
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
</#if>
<#if usesHibernateOnDelete>
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
</#if>

/**
 * ${(metaClass.javaDoc)!"Elemento generato automaticamente: " + metaClass.name}
<#if metaClass.since?? && metaClass.since?has_content>
 * @since ${metaClass.since}
</#if>
<#if metaClass.author?? && metaClass.author?has_content>
 * @author ${metaClass.author}
</#if>
 */
@Entity
@Table(name = "${resolveTableName(metaClass)}")
public class ${metaClass.name} {

    // --- ATTRIBUTI SEMPLICI ---
<#list fields as field>
    /**
     * ${(field.javaDoc)!"Elemento generato automaticamente: " + field.name}
<#if field.since?? && field.since?has_content>
     * @since ${field.since}
</#if>
     */
    <#if field.relation>
    <#if field.relationType == "MANY_TO_ONE">
    @ManyToOne(<#if field.required>optional = false<#if hasJpaCascade(field)>, </#if></#if><#if hasJpaCascade(field)>cascade = { ${resolveJpaCascade(field)} }</#if>)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name = "${resolveColumnName(field)}"<#if field.required>, nullable = false</#if>)
    <#if field.cascadeOnDelete?? && field.cascadeOnDelete == "CASCADE">
    @OnDelete(action = OnDeleteAction.CASCADE)
    </#if>
    private ${field.javaType} ${field.name};
    <#elseif field.relationType == "ONE_TO_ONE">
    @OneToOne(<#if field.required>optional = false<#if hasJpaCascade(field)>, </#if></#if><#if hasJpaCascade(field)>cascade = { ${resolveJpaCascade(field)} }</#if>)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name = "${resolveColumnName(field)}"<#if field.required>, nullable = false</#if>)
    <#if field.cascadeOnDelete?? && field.cascadeOnDelete == "CASCADE">
    @OnDelete(action = OnDeleteAction.CASCADE)
    </#if>
    private ${field.javaType} ${field.name};
    <#elseif field.relationType == "ONE_TO_MANY">
    @OneToMany(mappedBy = "${resolveMappedBy(field)}"<#if hasJpaCascade(field)>, cascade = { ${resolveJpaCascade(field)} }</#if>)
    @Fetch(FetchMode.SELECT)
    private List<${field.javaType}> ${field.name} = new ArrayList<>();
    <#elseif field.relationType == "MANY_TO_MANY">
    @ManyToMany(<#if !isOwningManyToMany(field)>mappedBy = "${resolveMappedBy(field)}"<#if hasJpaCascade(field)>, </#if></#if><#if hasJpaCascade(field)>cascade = { ${resolveJpaCascade(field)} }</#if>)
    @Fetch(FetchMode.SELECT)
    <#if isOwningManyToMany(field)>
    @JoinTable(
        name = "${resolveJoinTableName(field)}",
        joinColumns = @JoinColumn(name = "${resolveJoinColumnName(field)}"),
        inverseJoinColumns = @JoinColumn(name = "${resolveInverseJoinColumnName(field)}")
    )
    </#if>
    private List<${field.javaType}> ${field.name} = new ArrayList<>();
    <#else>
    private ${field.javaType} ${field.name};
    </#if>
    <#elseif field.collection>
    @Transient
    private List<${field.javaType}> ${field.name} = new ArrayList<>();
    <#else>
    <#if isIdField(field)>
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    </#if>
    @Column(name = "${resolveColumnName(field)}"<#if field.required>, nullable = false</#if><#if field.tags["unique"]?? && field.tags["unique"]?lower_case == "true">, unique = true</#if>)
    private ${field.javaType} ${field.name};
    </#if>
</#list>

    // --- COSTRUTTORE ---
    public ${metaClass.name}() {
    }

    // --- GETTER E SETTER ---
<#list fields as field>
    /**
     * Restituisce ${field.name}.
     *
     * @return ${((field.javaDoc)!"Elemento generato automaticamente: " + field.name)?uncap_first}
     */
    public <#if field.collection>List<${field.javaType}><#else>${field.javaType}</#if> get${field.name?cap_first}() {
        return this.${field.name};
    }

    /**
     * Imposta ${field.name}.
     *
     * @param ${field.name} ${((field.javaDoc)!"Elemento generato automaticamente: " + field.name)?uncap_first}
     */
    public void set${field.name?cap_first}(<#if field.collection>List<${field.javaType}><#else>${field.javaType}</#if> ${field.name}) {
        this.${field.name} = ${field.name};
    }
</#list>
}
