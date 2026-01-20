package com.example.demo;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.asentinel.common.orm.config.EnableAsentinelOrm;
import com.asentinel.common.orm.config.OrmConversionServiceConfig;
import com.example.demo.converters.InstantToTimestampConverter;
import com.example.demo.converters.TimestampToInstantConverter;

@EnableAsentinelOrm
@SpringBootApplication
public class DemoApplication {
	
	@Bean
	public DataSource dataSource() {
		return new SingleConnectionDataSource("jdbc:h2:mem:testdb", "sa", "", false);
	}
	
	@Bean
	public OrmConversionServiceConfig ormConversionServiceConfig() {
		return new OrmConversionServiceConfig() {

			@Override
			protected void registerConverters(ConfigurableConversionService conversionService) {
				conversionService.addConverter(new InstantToTimestampConverter());
				conversionService.addConverter(new TimestampToInstantConverter());
			}
			
		};
	}
	

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
