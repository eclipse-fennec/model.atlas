package org.eclipse.fennec.data.atlas.jpa.datasource;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.eclipse.fennec.data.atlas.jpa.datasource.helper.DataSourceConfigHelper;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.SqlDialect;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(immediate = true, name = "DataSourceConfigHandler")
public class DataSourceConfigHandler {


    @Reference
    private ConfigurationAdmin configAdmin;
    
    public static final Logger LOGGER = Logger.getLogger(DataSourceConfigHandler.class.getName());

    private final Map<Long, String> serviceIdToName = new ConcurrentHashMap<>();
    private final Map<String, Configuration> nameToConfig = new ConcurrentHashMap<>();

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        updated = "modifiedJpaMappingConfig",
        unbind = "unbindJpaMappingConfig"
    )
    void bindJpaMappingConfig(JpaMappingConfig config, Map<String, Object> props) {
        DataSourceConfig ds = config.getDataSource();
        if (ds == null || ds.getDialect() != SqlDialect.H2) {
        	LOGGER.warning(String.format("SqlDialect %s not supported for DataSource registration. Only H2 is currenlty supported"));
            return;
        }
        Long serviceId = (Long) props.get("service.id");
        String name = config.getName();
        String unitName = (String) props.get("unitName");
        try {
            Configuration cfg = DataSourceConfigHelper.createH2Config(configAdmin, name, unitName, ds);
            serviceIdToName.put(serviceId, name);
            nameToConfig.put(name, cfg);
            System.out.println("Registered DataSource for unitName " + unitName);
        } catch (IOException e) {
        	LOGGER.severe("Failed to create H2 DataSource config for '" + name + "': " + e.getMessage());
        }
    }



    void unbindJpaMappingConfig(JpaMappingConfig config, Map<String, Object> props) {
        Long serviceId = (Long) props.get("service.id");
        removeConfig(serviceId, serviceIdToName.get(serviceId));
        System.out.println("Unregistered DataSource for unitName " + (String) props.get("unitName"));
    }


    private void removeConfig(Long serviceId, String name) {
        if (serviceId != null) {
            serviceIdToName.remove(serviceId);
        }
        if (name != null) {
            Configuration cfg = nameToConfig.remove(name);
            if (cfg != null) {
                try {
                    cfg.delete();
                } catch (IOException e) {
                    LOGGER.severe("Failed to delete H2 DataSource config '" + name + "': " + e.getMessage());
                }
            }
        }
    }
}
