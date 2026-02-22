package com.harmoni.pos.order.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * Configuration class for setting up a MySQL data source with MyBatis for transaction management.
 * <p>
 * This configures the {@link DataSource}, {@link SqlSessionFactory}, and {@link DataSourceTransactionManager}
 * specifically for handling SQL-based transactions.
 * </p>
 */
@MapperScan(value = "com.harmoni.pos.order.mapper")
@Configuration
public class DbTransactionSQLConfig {

    /**
     * Defines the transaction-related MySQL {@link DataSource}.
     * <p>
     * The properties are loaded from {@code spring.datasource.transaction.mysql.*} in the configuration file.
     * </p>
     *
     * @return the configured {@link DataSource}
     */
    @Bean(name = "transactionSQLDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.transaction.mysql")
    public DataSource transactionSQLDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * Creates a {@link SqlSessionFactory} for MyBatis using the transaction SQL data source.
     *
     * @param transactionSQLDataSource the data source for transactional SQL operations
     * @param context                  the Spring application context (can be used to load MyBatis config/resources if needed)
     * @return the configured {@link SqlSessionFactory}
     * @throws Exception if initialization fails
     */
    @Bean(name = "transactionSqlSessionFactory")
    public SqlSessionFactory transactionSqlSessionFactory(
            @Qualifier("transactionSQLDataSource") DataSource transactionSQLDataSource,
            ApplicationContext context
    ) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(transactionSQLDataSource);
        return sqlSessionFactoryBean.getObject();
    }

    /**
     * Configures the transaction manager for the transaction SQL data source.
     *
     * @param menuSqlDatasource the data source to be managed by this transaction manager
     * @return the configured {@link DataSourceTransactionManager}
     */
    @Bean(name = "transactionSQLTransactionManager")
    public DataSourceTransactionManager primaryTransactionManager(
            @Qualifier("transactionSQLDataSource") DataSource menuSqlDatasource) {
        return new DataSourceTransactionManager(menuSqlDatasource);
    }
}
