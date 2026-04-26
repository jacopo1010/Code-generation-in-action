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
<#assign packageService = packageService!"jacopo.with.develop.service">
<#assign packageController = packageController!"jacopo.with.develop.controller">
<#assign allFields = metaClass.fields?values>
<#assign persistentFields = allFields?filter(field -> !field.collection && !field.relation)>
<#assign idFieldCandidates = persistentFields?filter(field -> field.name == "id" || resolveColumnName(field) == "id")>
<#assign idField = "">
<#if idFieldCandidates?size gt 0>
    <#assign idField = idFieldCandidates[0]>
</#if>
<#assign stringFields = persistentFields?filter(field -> field.javaType == "String")>
<#assign manyToOneFields = allFields?filter(field -> field.relation && field.relationType == "MANY_TO_ONE" && field.foreignKeyColumn?? && field.foreignKeyColumn?has_content)>
<#assign resourceName = toSqlName(entityName) + "s">

package ${packageController};

import java.net.URI;
import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ${packageModel}.${entityName};
import ${packageService}.${entityName}Service;

@Path("api/${resourceName}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ${entityName}ControllerBase {

    protected final ${entityName}Service ${entityName?uncap_first}Service;

    protected ${entityName}ControllerBase(${entityName}Service ${entityName?uncap_first}Service) {
        this.${entityName?uncap_first}Service = ${entityName?uncap_first}Service;
    }

    @GET
    public Response getAll${entityName}s() {
        List<${entityName}> ${entityName?uncap_first}s = this.${entityName?uncap_first}Service.findAll();
        return Response.ok(${entityName?uncap_first}s).build();
    }

    @GET
    @Path("/count")
    public Response count${entityName}s() {
        return Response.ok(this.${entityName?uncap_first}Service.count()).build();
    }

<#if idField?has_content>
    @GET
    @Path("/exists/{id}")
    public Response exists${entityName}(@PathParam("id") ${idField.javaType} id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok(this.${entityName?uncap_first}Service.existsById(id)).build();
    }

</#if>
<#if stringFields?size gt 0>
    @GET
    @Path("/search")
    public Response findByKeyword(@QueryParam("keyword") String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok(this.${entityName?uncap_first}Service.findByKeyword(keyword)).build();
    }

</#if>
<#if idField?has_content>
    @GET
    @Path("/{id}")
    public Response get${entityName}(@PathParam("id") ${idField.javaType} id) {
        Optional<${entityName}> ${entityName?uncap_first} = this.${entityName?uncap_first}Service.findById(id);
        if (!${entityName?uncap_first}.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(${entityName?uncap_first}.get()).build();
    }

</#if>
    @POST
    public Response create${entityName}(${entityName} ${entityName?uncap_first}) {
        try {
            ${entityName} created = this.${entityName?uncap_first}Service.save(${entityName?uncap_first});
<#if idField?has_content>
            if (created.get${idField.name?cap_first}() == null) {
                return Response.serverError().build();
            }
            return Response.created(URI.create("/api/${resourceName}/" + created.get${idField.name?cap_first}())).entity(created).build();
<#else>
            return Response.status(Response.Status.CREATED).entity(created).build();
</#if>
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
        }
    }

<#if idField?has_content>
    @PUT
    @Path("/{id}")
    public Response update${entityName}(@PathParam("id") ${idField.javaType} id, ${entityName} ${entityName?uncap_first}) {
        if (${entityName?uncap_first} == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            ${entityName?uncap_first}.set${idField.name?cap_first}(id);
            boolean updated = this.${entityName?uncap_first}Service.update(${entityName?uncap_first});
            if (!updated) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            return Response.ok(${entityName?uncap_first}).build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
        }
    }

</#if>
    @DELETE
    public Response deleteAll() {
        this.${entityName?uncap_first}Service.deleteAll();
        return Response.noContent().build();
    }

<#if idField?has_content>
    @DELETE
    @Path("/{id}")
    public Response deleteById(@PathParam("id") ${idField.javaType} id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        boolean deleted = this.${entityName?uncap_first}Service.delete(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

</#if>
<#list manyToOneFields as relationField>
    <#assign relationIdField = resolveRelationIdField(relationField)>
    <#assign relationIdType = "Long">
    <#if relationIdField?has_content>
        <#assign relationIdType = relationIdField.javaType>
    </#if>
    @GET
    @Path("/by-${toSqlName(relationField.name)}/{${relationField.name}Id}")
    public Response findBy${relationField.name?cap_first}Id(@PathParam("${relationField.name}Id") ${relationIdType} ${relationField.name}Id) {
        if (${relationField.name}Id == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok(this.${entityName?uncap_first}Service.findBy${relationField.name?cap_first}Id(${relationField.name}Id)).build();
    }

</#list>
}
