package com.example.demo;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.SqlQueryTemplate;
import com.asentinel.common.jdbc.flavors.CustomArgumentPreparedStatementSetter;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.h2.H2JdbcFlavor;
import com.asentinel.common.orm.OrmOperations;
import com.asentinel.common.orm.OrmTemplate;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.DefaultSqlBuilderFactory;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.persist.SimpleUpdater;
import com.asentinel.common.orm.query.DefaultSqlFactory;
import com.asentinel.common.orm.query.SqlFactory;
import com.example.demo.converters.InstantToTimestampConverter;
import com.example.demo.converters.TimestampToInstantConverter;


@SpringBootApplication
public class DemoApplication {
	
	@Bean
	public DataSource dataSource() {
		return new SingleConnectionDataSource("jdbc:h2:mem:testdb", "sa", "", false);
	}

    @Bean
    public JdbcFlavor jdbcFlavor() {
        return new H2JdbcFlavor();
    }
	
	@Bean
	public JdbcOperations jdbcOperations(DataSource dataSource, JdbcFlavor jdbcFlavor) {
		return new JdbcTemplate(dataSource) {
			
			/*
			 * add support for byte[], InputStream and Enum params
			 */
			@Override
			protected PreparedStatementSetter newArgPreparedStatementSetter(Object[] args) {
				return new CustomArgumentPreparedStatementSetter(jdbcFlavor, args);
			}
		};
	}
	
    @Bean
    public SqlQuery sqlQuery(JdbcFlavor jdbcFlavor, JdbcOperations jdbcOps) {
        return new SqlQueryTemplate(jdbcFlavor, jdbcOps);
    }

    @Bean
    public SqlFactory sqlFactory(JdbcFlavor jdbcFlavor) {
        return new DefaultSqlFactory(jdbcFlavor);
    }

    @Bean
    public DefaultEntityDescriptorTreeRepository entityDescriptorTreeRepository(SqlBuilderFactory sqlBuilderFactory, ConversionService conversionService) {
        DefaultEntityDescriptorTreeRepository treeRepository = new DefaultEntityDescriptorTreeRepository();
        treeRepository.setSqlBuilderFactory(sqlBuilderFactory);
        treeRepository.setConversionService(conversionService);
        return treeRepository;
    }

    @Bean
    public DefaultSqlBuilderFactory sqlBuilderFactory(@Lazy EntityDescriptorTreeRepository entityDescriptorTreeRepository,
                                                      SqlFactory sqlFactory, SqlQuery sqlQuery) {
        DefaultSqlBuilderFactory sqlBuilderFactory = new DefaultSqlBuilderFactory(sqlFactory, sqlQuery);
        sqlBuilderFactory.setEntityDescriptorTreeRepository(entityDescriptorTreeRepository);
        return sqlBuilderFactory;
    }

    @Bean
    public ConversionService ormConversionService() {
    	GenericConversionService conversionService = new GenericConversionService();
    	conversionService.addConverter(new TimestampToInstantConverter());
    	conversionService.addConverter(new InstantToTimestampConverter());
    	return conversionService;
    }
    
    @Bean
    public OrmOperations orm(JdbcFlavor jdbcFlavor, SqlQuery sqlQuery,
                             SqlBuilderFactory sqlBuilderFactory, ConversionService conversionService) {
       	SimpleUpdater updater = new SimpleUpdater(jdbcFlavor, sqlQuery);
    	updater.setConversionService(conversionService);
        return new OrmTemplate(sqlBuilderFactory, updater);
    }

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
