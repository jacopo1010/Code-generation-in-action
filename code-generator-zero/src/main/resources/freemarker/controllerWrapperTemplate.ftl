<#ftl output_format="plainText">
<#assign entityName = metaClass.name>
package ${packageController};

import ${packageService}.${entityName}Service;

public class ${entityName}Controller extends ${entityName}ControllerBase {

    public ${entityName}Controller(${entityName}Service ${entityName?uncap_first}Service) {
        super(${entityName?uncap_first}Service);
    }
}
