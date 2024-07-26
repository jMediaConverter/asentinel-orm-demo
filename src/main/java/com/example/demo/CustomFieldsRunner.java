package com.example.demo;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.asentinel.common.jdbc.DefaultObjectFactory;
import com.asentinel.common.orm.OrmOperations;
import com.asentinel.common.orm.mappers.dynamic.DefaultDynamicColumn;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumnsEntityNodeCallback;
import com.asentinel.common.orm.persist.UpdateSettings;
import com.example.demo.domain.CarManufacturer;
import com.example.demo.domain.CustomFieldsCarManufacturer;

import jakarta.annotation.PostConstruct;

@Component
public class CustomFieldsRunner {
	private final static Log logger = LogFactory.getLog(CustomFieldsRunner.class);

	private final DefaultDynamicColumn colCountry = new DefaultDynamicColumn("country", String.class);
	private final DefaultDynamicColumn colFoundationYear = new DefaultDynamicColumn("foundationYear", Integer.class);
	
	private final OrmOperations orm;
	
	public CustomFieldsRunner(OrmOperations orm) {
		this.orm = orm;
	}
	
	
	@PostConstruct
	public void postConstruct() {
		addCustomFields();
		
		persistSomeData();
		
		loadSomeData();
	}
	
	private void addCustomFields() {
		logger.info("\n\nadd custom fields ...");
		orm.getSqlQuery()
			.update("alter table CarManufacturers add column Country varchar(255)");
		orm.getSqlQuery()
			.update("alter table CarManufacturers add column FoundationYear int");
	}
	
	private void persistSomeData() {
		logger.info("\n\npersist some data ...");
		CustomFieldsCarManufacturer bmw = new CustomFieldsCarManufacturer("BMW", 
				Map.of(
						colCountry, "Germany", 
						colFoundationYear, 1905));
		orm.update(bmw, new UpdateSettings<>(List.of(colCountry, colFoundationYear)));
	}
	
	private void loadSomeData() {
		logger.info("\n\nload some data ...");
		CustomFieldsCarManufacturer bmw = orm.newSqlBuilder(CustomFieldsCarManufacturer.class)
			.select(
				new DynamicColumnsEntityNodeCallback<DefaultDynamicColumn, CustomFieldsCarManufacturer>(
					new DefaultObjectFactory<>(CustomFieldsCarManufacturer.class),
					List.of(colCountry, colFoundationYear)
				)
			).where().column(CarManufacturer.COL_NAME).eq("BMW")
			.execForEntity();
		logger.info(bmw);
	}
}
