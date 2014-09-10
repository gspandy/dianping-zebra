package com.dianping.zebra.group.datasources;

import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.filter.JdbcFilter;
import com.dianping.zebra.group.filter.JdbcMetaData;
import com.dianping.zebra.group.jdbc.AbstractDataSource;
import com.dianping.zebra.group.monitor.SingleDataSourceMBean;
import com.dianping.zebra.group.router.DataSourceRouter;
import com.dianping.zebra.group.router.RouterContext;
import com.dianping.zebra.group.router.RouterTarget;
import com.dianping.zebra.group.router.WeightDataSourceRouter;
import com.dianping.zebra.group.util.JDBCExceptionUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class LoadBalancedDataSource extends AbstractDataSource {

	private SingleDataSourceManager dataSourceManager;

	private Map<String, SingleDataSource> dataSources;

	private Map<String, DataSourceConfig> loadBalancedConfigMap;

	private int retryTimes;

	private DataSourceRouter router;

	public LoadBalancedDataSource(Map<String, DataSourceConfig> loadBalancedConfigMap,
			JdbcMetaData metaData, JdbcFilter filter, int retryTimes) {
		this.dataSources = new HashMap<String, SingleDataSource>();
		this.loadBalancedConfigMap = loadBalancedConfigMap;
		this.retryTimes = retryTimes;
		this.filter = filter;
		this.metaData = metaData;
	}

	public void close() throws SQLException {
		for (SingleDataSource ds : dataSources.values()) {
			dataSourceManager.destoryDataSource(ds);
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		RouterContext context = new RouterContext();

		for (SingleDataSource dataSource : this.dataSources.values()) {
			if (dataSource.isDown() || dataSource.isClosed()) {
				context.addExcludeTarget(dataSource.getId());
			}
		}

		RouterTarget target = this.router.select(context);

		if (target != null) {
			int tmpRetryTimes = -1;
			Set<RouterTarget> excludeTargets = new HashSet<RouterTarget>();
			List<SQLException> exceptions = new ArrayList<SQLException>();

			while (tmpRetryTimes++ < this.retryTimes) {
				try {
					return this.dataSources.get(target.getId()).getConnection();
				} catch (SQLException e) {
					exceptions.add(e);
					excludeTargets.add(target);
					context = new RouterContext(excludeTargets);
					target = router.select(context);
					if (target == null) {
						break;
					}
				}
			}

			if (!exceptions.isEmpty()) {
				JDBCExceptionUtils.throwSQLExceptionIfNeeded(exceptions);
			}
		} else {
			throw new SQLException("No available dataSource");
		}

		throw new SQLException("Can not aquire connection");
	}

	public Map<String, SingleDataSourceMBean> getCurrentDataSourceMBean() {
		Map<String, SingleDataSourceMBean> beans = new HashMap<String, SingleDataSourceMBean>();
		beans.putAll(dataSources);

		return beans;
	}

	public void init() {
		initFilter();

		this.dataSourceManager = SingleDataSourceManagerFactory.getDataSourceManager();

		for (DataSourceConfig config : loadBalancedConfigMap.values()) {
			SingleDataSource dataSource = dataSourceManager.createDataSource(config, this.metaData.clone(), this.filter);
			this.dataSources.put(config.getId(), dataSource);
		}

		this.router = new WeightDataSourceRouter(loadBalancedConfigMap);
	}

	private void initFilter() {
		this.metaData.setDataSource(this);
	}
}
