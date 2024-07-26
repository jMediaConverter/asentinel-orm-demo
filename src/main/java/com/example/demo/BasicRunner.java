package com.example.demo;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;

import com.asentinel.common.orm.AutoEagerLoader;
import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.OrmOperations;
import com.asentinel.common.orm.jql.Page;
import com.example.demo.domain.CarManufacturer;
import com.example.demo.domain.CarModel;
import com.example.demo.domain.CarType;

import jakarta.annotation.PostConstruct;

@Component
public class BasicRunner {
	private final static Log logger = LogFactory.getLog(BasicRunner.class);

	private final OrmOperations orm;
	
	public BasicRunner(OrmOperations orm) {
		this.orm = orm;
	}
	
	
	@PostConstruct
	public void postConstruct() {
		persistSomeData();
		
		loadSomeData();
		
		loadSomeDataEagerly();
		
		loadEntity();
		
		paginationWithSimpleAssociation();
		
		paginationWithCollection();
		
		slightlyComplexQueries();
		
		// easy fallback to JdbcOperations
		JdbcOperations jdbcOps = orm.getSqlQuery().getJdbcOperations();
		int count = jdbcOps.queryForObject("select count(*) from CarManufacturers", Integer.class);
		logger.info("Manufacturers count: " + count);
	}
	
	
	private void persistSomeData() {
		logger.info("\n\npersist some data ...");
		CarManufacturer mazda = new CarManufacturer("Mazda");
		orm.update(mazda);
		CarModel mx5 = new CarModel("mx5", CarType.CAR, mazda);
		CarModel m3 = new CarModel("3", CarType.CAR, mazda);
		CarModel m6 = new CarModel("6", CarType.CAR, mazda);
		CarModel cx3 = new CarModel("cx3", CarType.SUV, mazda);
		orm.update(mx5, m3, m6, cx3);
		
		CarManufacturer honda = new CarManufacturer("Honda");
		orm.update(honda);
		
		CarModel accord = new CarModel("accord", CarType.CAR, honda);
		orm.update(accord);
		
		CarModel civic = new CarModel("civic", CarType.CAR, honda);
		CarModel crv = new CarModel("crv", CarType.SUV, honda);
		orm.update(civic, crv);
		
		CarManufacturer toyota = new CarManufacturer("Toyota");
		orm.update(toyota);
		orm.delete(CarManufacturer.class, toyota.getId());
	}
	
	private void loadSomeData() {
		logger.info("\n\nload some data ...");
		List<CarModel> models = orm.newSqlBuilder(CarModel.class)
				.select().orderBy().column(CarModel.COL_NAME)
				.exec();
		for (CarModel model: models) {
			logger.info(model);
			// check the manufacturer is lazily loaded, it is a proxy
			if (EntityUtils.isProxy(model.getCarManufacturer()) && EntityUtils.isLoadedProxy(model.getCarManufacturer())) {
				throw new RuntimeException("Something is wrong !");
			}
		}
	}
	
	private void loadSomeDataEagerly() {
		logger.info("\n\nload some data eagerly ...");
		List<CarModel> models = orm.newSqlBuilder(CarModel.class)
				.select(
					AutoEagerLoader.forPath(CarModel.class, CarManufacturer.class)
				)
				.orderBy().column(CarModel.COL_NAME)
				.exec();
		for (CarModel model: models) {
			logger.info(model);
			// check the manufacturer is eagerly loaded, it is NOT a proxy
			if (EntityUtils.isProxy(model.getCarManufacturer())) {
				throw new RuntimeException("Something is wrong !");
			}
		}
	}

	private void loadEntity() {
		logger.info("\n\nload entity ...");
		CarManufacturer ford = new CarManufacturer("Ford");
		orm.update(ford);
		
		int fordId = ford.getId();
		
		// load another ford entity
		CarManufacturer ford2 = orm.getEntity(CarManufacturer.class, fordId);
		logger.info("Ford: " + ford2);
		
		// load a proxy for the ford entity
		CarManufacturer fordProxy = orm.getProxy(CarManufacturer.class, fordId);
		if (!EntityUtils.isProxy(fordProxy)) {
			throw new RuntimeException("Something is wrong !");
		}
		if (EntityUtils.isLoadedProxy(fordProxy)) {
			throw new RuntimeException("Something is wrong !");
		}
		
		// load the proxy
		logger.info("Name: " + fordProxy.getName());

	}
	
	private void paginationWithSimpleAssociation() {
		logger.info("\n\npaginationWithSimpleAssociation ...");
		Page<CarModel> page = orm.newSqlBuilder(CarModel.class)
			.pagedSelect(3, 5)
			.pagedOrderBy()
				.column(CarModel.COL_NAME)
			.execForPage();
		logger.info("Page: " + page);
		for (CarModel model: page.getItems()) {
			logger.info(model);
		}
	}
	
	private void paginationWithCollection() {
		logger.info("\n\npaginationWithCollection ...");
		Page<CarManufacturer> page = orm.newSqlBuilder(CarManufacturer.class)
				.pagedSelect(1, 3, AutoEagerLoader.forPath(CarManufacturer.class, CarModel.class))
				.pagedOrderBy()
					.column(CarManufacturer.COL_NAME)
				.execForPage();

		logger.info("Page: " + page);
		for (CarManufacturer man: page.getItems()) {
			logger.info(man);
			for (CarModel model: man.getModels()) {
				logger.info("   " + model);
			}
		}
	}
	
	private void slightlyComplexQueries() {
		logger.info("\n\nslightlyComplexQueries ...");
		CarModel mx5 = orm.newSqlBuilder(CarModel.class)
			.select()
			.where()
				.column(CarModel.COL_NAME).eq("mx5")
			.execForEntity();
		logger.info(mx5);
		
		CarModel crv = orm.newSqlBuilder(CarModel.class)
				.select()
				.where().upperColumn(CarModel.COL_NAME).eq("CRV")
				.execForEntity();
		logger.info(crv);		
		
		CarModel cx3 = orm.newSqlBuilder(CarModel.class)
				.select(AutoEagerLoader.forPath(CarModel.class, CarManufacturer.class))
				.where()
						.column(CarModel.COL_NAME).eq("cx3")
					.and()
						.alias(CarModel.class, CarManufacturer.class) // reference the CarManufacturers table for further conditions
							.sql("upper(").column(CarManufacturer.COL_NAME).sql(")").eq("MAZDA") // note the use of plain SQL
					.and()
						.rootAlias() // reference the root CarModels table
						.id().gt(0)
					.and()
						.column(CarModel.COL_TYPE).eq(CarType.SUV)
				.execForEntity();
		logger.info(cx3);
	}
}
  