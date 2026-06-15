package ch.bbw.pr.tresorbackend;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;

/**
 * LazyDataSourceConfig
 *   Do not establish connection unless first data access.
 * @author Peter Rutschmann
 */
@Configuration
public class LazyDataSourceConfig {
    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        DataSource realDataSource = properties.initializeDataSourceBuilder().build();
        return new LazyConnectionDataSourceProxy(realDataSource);
    }
}
