package ${packageName!"jacopo.with.develop.model"};

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "${metaClass.table?has_content?then(metaClass.table, metaClass.name?replace('([a-z0-9])([A-Z])', '$1_$2', 'r')?replace('[^A-Za-z0-9_]', '_', 'r')?lower_case)}")
public class ${metaClass.name} extends ${metaClass.name}Base {

    public ${metaClass.name}() {
        super();
    }
}
