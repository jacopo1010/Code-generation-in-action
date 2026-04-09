<#ftl ns_prefixes={"xmi":"http://schema.omg.org/spec/XMI/2.1", "uml":"http://schema.omg.org/spec/UML/2.0"}>
<#-- 1. Funzione per tradurre i tipi XMI in tipi Java validi -->
<#function mappaTipo xmiType>
    <#if xmiType == "string_id">
        <#return "String">
    <#elseif xmiType == "long_id">
        <#return "Long">
    <#elseif xmiType == "boolean_id">
        <#return "Boolean">
    <#elseif xmiType == "timestamp_id">
        <#return "LocalDateTime">
    <#else>
        <#return "Object">
    </#if>
</#function>

<#-- 2. Navighiamo l'XML e cerchiamo SOLO la classe che corrisponde a "classeCorrente" -->
<#list doc["xmi:XMI"]["uml:Model"]["packagedElement"]["packagedElement"] as elemento>
<#if elemento["@xmi:type"] == "uml:Class" && elemento.@name == classeCorrente>
package jacopo.with.develop.model;

<#-- Importiamo le librerie base se necessarie -->
import java.time.LocalDateTime;

/**
 * Classe generata automaticamente dal modello UML.
 */
public class ${elemento.@name} {

<#-- 3. CICLO SUI CAMPI (Attributi della classe) -->
<#list elemento["ownedAttribute"] as attributo>
    private ${mappaTipo(attributo.@type)} ${attributo.@name};
</#list>

<#-- 4. CICLO PER GENERARE GETTER E SETTER -->
<#list elemento["ownedAttribute"] as attributo>
    public ${mappaTipo(attributo.@type)} get${attributo.@name?cap_first}() {
        return this.${attributo.@name};
    }

    public void set${attributo.@name?cap_first}(${mappaTipo(attributo.@type)} ${attributo.@name}) {
        this.${attributo.@name} = ${attributo.@name};
    }

</#list>
}
</#if>
</#list>