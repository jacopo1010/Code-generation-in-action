<#ftl ns_prefixes={"xmi":"http://schema.omg.org/spec/XMI/2.1", "uml":"http://schema.omg.org/spec/UML/2.0"}>

<#-- 1. Funzione per mappare i tipi base di StarUML in Java -->
<#function mappaTipo xmiType>
    <#if xmiType == "string_id"><#return "String">
    <#elseif xmiType == "long_id"><#return "Long">
    <#elseif xmiType == "boolean_id"><#return "Boolean">
    <#elseif xmiType == "timestamp_id"><#return "LocalDateTime">
    <#else><#return "Object">
    </#if>
</#function>

<#-- 2. NUOVA FUNZIONE: Motore di ricerca! 
     Dato un ID es. "AAAAAAGdaPo...", cerca nel documento e restituisce il nome "Project" -->
<#function trovaNomeClassePerId idClasseTarget>
    <#list doc["xmi:XMI"]["uml:Model"]["packagedElement"]["packagedElement"] as classe>
        <#if classe["@xmi:type"] == "uml:Class" && classe["@xmi:id"] == idClasseTarget>
            <#return classe.@name>
        </#if>
    </#list>
    <#return "Object"> <#-- Sicurezza nel caso l'ID non si trovi -->
</#function>

<#-- 3. Inizia l'esplorazione per la classe che Java ci ha chiesto di generare -->
<#list doc["xmi:XMI"]["uml:Model"]["packagedElement"]["packagedElement"] as elemento>
<#if elemento["@xmi:type"] == "uml:Class" && elemento.@name == classeCorrente>
<#assign idClasseCorrente = elemento["@xmi:id"]>
package jacopo.with.develop.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Classe generata automaticamente dal modello UML di Jacopo.
 */
public class ${elemento.@name} {

    // --- ATTRIBUTI SEMPLICI ---
<#list elemento["ownedAttribute"] as attributo>
    private ${mappaTipo(attributo.@type)} ${attributo.@name};
</#list>

    // --- RELAZIONI (Associazioni UML) ---
<#list elemento["ownedMember"] as relazione>
    <#if relazione["@xmi:type"] == "uml:Association">
        
        <#-- L'associazione ha due capi (ownedEnd). Cicliamo per trovare quello che punta all'ALTRA classe -->
        <#list relazione["ownedEnd"] as capo>
            <#if capo["@type"] != idClasseCorrente>
                <#assign targetClassId = capo["@type"]>
                <#assign targetClassName = trovaNomeClassePerId(targetClassId)>
                <#assign cardinalitaMax = capo["upperValue"].@value>
                <#assign nomeVariabile = targetClassName?uncap_first> <#-- Rende "Project" -> "project" -->

                <#-- Controllo della cardinalità: Se c'è l'asterisco generiamo una Lista! -->
                <#if cardinalitaMax == "*">
    private List<${targetClassName}> ${nomeVariabile}List = new ArrayList<>();
                <#else>
    private ${targetClassName} ${nomeVariabile};
                </#if>
            </#if>
        </#list>
    </#if>
</#list>

    // --- GETTER E SETTER (Attributi) ---
<#list elemento["ownedAttribute"] as attributo>
    public ${mappaTipo(attributo.@type)} get${attributo.@name?cap_first}() {
        return this.${attributo.@name};
    }

    public void set${attributo.@name?cap_first}(${mappaTipo(attributo.@type)} ${attributo.@name}) {
        this.${attributo.@name} = ${attributo.@name};
    }
</#list>

    // --- GETTER E SETTER (Relazioni) ---
<#list elemento["ownedMember"] as relazione>
    <#if relazione["@xmi:type"] == "uml:Association">
        <#list relazione["ownedEnd"] as capo>
            <#if capo["@type"] != idClasseCorrente>
                <#assign targetClassName = trovaNomeClassePerId(capo["@type"])>
                <#assign cardinalitaMax = capo["upperValue"].@value>
                <#assign nomeVar = targetClassName?uncap_first>
                
                <#if cardinalitaMax == "*">
    public List<${targetClassName}> get${targetClassName}List() {
        return this.${nomeVar}List;
    }

    public void set${targetClassName}List(List<${targetClassName}> ${nomeVar}List) {
        this.${nomeVar}List = ${nomeVar}List;
    }
                <#else>
    public ${targetClassName} get${targetClassName}() {
        return this.${nomeVar};
    }

    public void set${targetClassName}(${targetClassName} ${nomeVar}) {
        this.${nomeVar} = ${nomeVar};
    }
                </#if>
            </#if>
        </#list>
    </#if>
</#list>
}
</#if>
</#list>