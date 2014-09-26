package com.dianping.zebra.group;

import com.dianping.zebra.group.config.DataSourceConfigManagerTest;
import com.dianping.zebra.group.config.LocalConfigServiceTest;
import com.dianping.zebra.group.config.SystemConfigManagerTest;
import com.dianping.zebra.group.datasources.FailoverDataSourceTest;
import com.dianping.zebra.group.filter.DefaultFilterManagerTest;
import com.dianping.zebra.group.filter.JdbcMetaDataTest;
import com.dianping.zebra.group.jdbc.*;
import com.dianping.zebra.group.router.CustomizedReadWriteStrategyWrapperTest;
import com.dianping.zebra.group.router.DpdlReadWriteStrategyImplTest;
import com.dianping.zebra.group.router.GroupDataSourceRouter;
import com.dianping.zebra.group.util.AtomicRefreshTest;
import com.dianping.zebra.group.util.SmoothReloadTest;
import com.dianping.zebra.group.util.SqlUtilsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({

		//config
		DataSourceConfigManagerTest.class,
		LocalConfigServiceTest.class,
		SystemConfigManagerTest.class,

		//datasources
		FailoverDataSourceTest.class,

		//mockedFilter
		JdbcMetaDataTest.class,
		DefaultFilterManagerTest.class,

		//jdbc
		DPGroupConnectionTestCase.class,
		DPGroupPreparedStatementTest.class,
		DPGroupStatementTest.class,
		SingleAndGroupC3P0FieldTest.class,
		GroupDataSourceTest.class,

		//router
		CustomizedReadWriteStrategyWrapperTest.class,
		DpdlReadWriteStrategyImplTest.class,
		GroupDataSourceRouter.class,

		//util
		AtomicRefreshTest.class,
		SmoothReloadTest.class,
		SqlUtilsTest.class
})
public class AllTests {

}
