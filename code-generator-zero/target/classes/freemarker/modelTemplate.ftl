package ${packageName!"jacopo.with.develop.model"};

<#assign fields = metaClass.fields?values>
<#assign collectionFields = fields?filter(field -> field.collection)>
<#assign usesTimestamp = fields?filter(field -> field.javaType == "Timestamp")?size gt 0>
<#if usesTimestamp>
import java.sql.Timestamp;
</#if>
<#if collectionFields?size gt 0>
import java.util.List;
import java.util.ArrayList;
</#if>

/**
 * ${metaClass.javaDoc}
<#if metaClass.since?? && metaClass.since?has_content>
 * @since ${metaClass.since}
</#if>
<#if metaClass.author?? && metaClass.author?has_content>
 * @author ${metaClass.author}
</#if>
 */
public class ${metaClass.name} {

    // --- ATTRIBUTI SEMPLICI ---
<#list fields as field>
    /**
     * ${field.javaDoc}
<#if field.since?? && field.since?has_content>
     * @since ${field.since}
</#if>
     */
    <#if field.collection>
    private List<${field.javaType}> ${field.name} = new ArrayList<>();
    <#else>
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
     * @return ${field.javaDoc?uncap_first}
     */
    public <#if field.collection>List<${field.javaType}><#else>${field.javaType}</#if> get${field.name?cap_first}() {
        return this.${field.name};
    }

    /**
     * Imposta ${field.name}.
     *
     * @param ${field.name} ${field.javaDoc?uncap_first}
     */
    public void set${field.name?cap_first}(<#if field.collection>List<${field.javaType}><#else>${field.javaType}</#if> ${field.name}) {
        this.${field.name} = ${field.name};
    }
</#list>
}
