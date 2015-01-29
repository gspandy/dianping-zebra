package com.dianping.zebra.admin.mock;

import com.dianping.zebra.Constants;
import com.dianping.zebra.group.config.ConfigService;
import com.dianping.zebra.group.config.DefaultDataSourceConfigManager;
import com.dianping.zebra.group.config.SystemConfigManagerFactory;
import com.dianping.zebra.group.jdbc.GroupDataSource;

/**
 * Created by Dozer on 11/13/14.
 */
public class GroupDataSourceExtend extends GroupDataSource {

	@Override
	protected void initConfig() {
		this.groupConfig = buildGroupConfig();
		this.systemConfigManager = SystemConfigManagerFactory
				.getConfigManger(configManagerType, Constants.DEFAULT_SYSTEM_RESOURCE_ID);
	}

	public void setDataSourceConfigManager(ConfigService configService) {
		this.dataSourceConfigManager = new DefaultDataSourceConfigManager(jdbcRef, configService);
		this.dataSourceConfigManager.init();
	}
}
