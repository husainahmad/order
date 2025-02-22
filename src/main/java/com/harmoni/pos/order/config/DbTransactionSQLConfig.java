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

@MapperScan(value = "com.harmoni.pos.order.mapper")
@Configuration
public class DbTransactionSQLConfig {

    @Bean(name = "transactionSQLDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.transaction.mysql")
    public DataSource transactionSQLDataSource() {
        return DataSourceBuilder
                .create()
                .build();
    }

    @Bean(name = "transactionSqlSessionFactory")
    public SqlSessionFactory transactionSqlSessionFactory(
            @Qualifier("transactionSQLDataSource") DataSource transactionSQLDataSource,
            ApplicationContext context
    ) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(transactionSQLDataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "transactionSQLTransactionManager")
    public DataSourceTransactionManager primaryTransactionManager (
            @Qualifier("transactionSQLDataSource") DataSource menuSqlDatasource) {
        return new DataSourceTransactionManager(menuSqlDatasource);
    }
}
