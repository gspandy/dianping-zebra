package com.dianping.zebra.group.datasources;

import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.filter.JdbcFilter;
import com.dianping.zebra.group.filter.JdbcContext;

import java.util.List;

public interface SingleDataSourceManager {

	public SingleDataSource createDataSource(DataSourceConfig config, JdbcContext context, List<JdbcFilter> filter);

	public void destoryDataSource(SingleDataSource dataSource);

	public void init();
}