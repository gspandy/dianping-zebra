package com.dianping.zebra.monitor.spring;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ClassUtils;

import com.dianping.cat.Cat;
import com.dianping.zebra.Constants;
import com.dianping.zebra.exception.ZebraConfigException;
import com.dianping.zebra.group.config.DataSourceConfigManager;
import com.dianping.zebra.group.config.DataSourceConfigManagerFactory;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.group.router.RouterType;
import com.dianping.zebra.log.LoggerLoader;
import com.dianping.zebra.monitor.model.DataSourceInfo;
import com.dianping.zebra.monitor.util.LionUtil;
import com.dianping.zebra.util.StringUtils;
import com.google.gson.Gson;

public class DataSourceAutoReplacer implements BeanFactoryPostProcessor, PriorityOrdered {

	private static final Logger logger = LoggerLoader.getLogger(DataSourceAutoReplacer.class);

	// datasource class name
	private static final String C3P0_CLASS_NAME = "com.mchange.v2.c3p0.ComboPooledDataSource";

	private static final String DPDL_CLASS_NAME = "com.dianping.dpdl.sql.DPDataSource";

	private static final String GROUP_CLASS_NAME = "com.dianping.zebra.group.jdbc.GroupDataSource";

	private static final String SHARD_CLASS_NAME = "com.dianping.zebra.shard.jdbc.ShardDataSource";

	private static final String UPLOAD_DS_INFO_KEY = "zebra.server.heartbeat.url";

	// datasource set
	private Set<String> c3p0Ds = new HashSet<String>();

	private Set<String> dpdlDs = new HashSet<String>();

	private Set<String> groupDs = new HashSet<String>();

	private Set<String> shardDs = new HashSet<String>();

	private Set<String> otherDs = new HashSet<String>();

	private List<DataSourceInfo> infos = new ArrayList<DataSourceInfo>();

	private static boolean hasProcessd;

	private Map<String, String> canReplacedDatabase = new HashMap<String, String>();

	private DefaultListableBeanFactory listableBeanFactory = null;

	private Pattern pattern = Pattern.compile("^jdbc:([a-zA-Z0-9]+)://([^/]+)/([^?]+).*$");

	private void buildDataSourceInfo(DataSourceInfo info, BeanDefinition bean) {
		getJdbcUrlInfo(info, LionUtil.getLionValueFromBean(bean, "jdbcUrl"));
		info.setUsername(LionUtil.getLionValueFromBean(bean, "user"));
		info.setInitPoolSize(LionUtil.getLionValueFromBean(bean, "initialPoolSize"));
		info.setMaxPoolSize(LionUtil.getLionValueFromBean(bean, "maxPoolSize"));
		info.setMinPoolSize(LionUtil.getLionValueFromBean(bean, "minPoolSize"));
	}

	private boolean canReplace(DataSourceInfo info) {
		String configString = LionUtil.getLionConfig("groupds.autoreplace.database");

		if (StringUtils.isBlank(info.getDatabase()) || StringUtils.isBlank(configString)) {
			return false;
		}

		List<String> dataBases = Arrays.asList(configString.split(","));

		for (String key : dataBases) {
			canReplacedDatabase.put(key.toLowerCase(), key);
		}

		return canReplacedDatabase.containsKey(info.getDatabase());
	}

	private Boolean checkBeanIsRead(BeanDefinition bean) {
		String value = LionUtil.getLionValueFromBean(bean, "user");
		return value == null ? null : value.endsWith("_r");
	}

	private String getBeanPropertyStringValue(Object tempValue) {
		if (tempValue == null) {
			return null;
		}
		return tempValue instanceof TypedStringValue ? ((TypedStringValue) tempValue).getValue()
				: String.valueOf(tempValue);
	}

	private Set<PropertyValue> getC3P0PropertyValues(BeanDefinition c3p0BeanDefinition, DataSourceInfo info,
			RouterType routerType) {
		Set<PropertyValue> properties = new HashSet<PropertyValue>();

		if (!c3p0BeanDefinition.getBeanClassName().equals(C3P0_CLASS_NAME)) {
			return properties;
		} else {
			buildDataSourceInfo(info, c3p0BeanDefinition);

			if ("mysql".equals(info.getType()) && canReplace(info)) {
				String jdbcRef = canReplacedDatabase.get(info.getDatabase());
				String groupConfig = LionUtil.getLionConfig(String.format("groupds.%s.mapping", jdbcRef));
				if (!StringUtils.isBlank(groupConfig)) {
					properties.add(new PropertyValue("jdbcRef", jdbcRef));
					properties.add(new PropertyValue("extraJdbcUrlParams", parseUrlExtra(info.getUrl())));
					properties.add(new PropertyValue("routerType", routerType.getRouterType()));

					Set<String> ignoreList = getGroupDataSourceIgnoreProperties();
					for (PropertyValue property : c3p0BeanDefinition.getPropertyValues().getPropertyValues()) {
						if (ignoreList.contains(property.getName())) {
							continue;
						}
						properties.add(property);
					}
				}
			}

			return properties;
		}
	}

	private List<BeanDefinition> getDpdlReadDsBean(BeanDefinition dataSourceDefinition) {
		List<BeanDefinition> beans = new ArrayList<BeanDefinition>();

		PropertyValue pv = dataSourceDefinition.getPropertyValues().getPropertyValue("readDS");
		if (pv != null && pv.getValue() != null) {
			ManagedMap map = (ManagedMap) pv.getValue();
			for (Object item : map.keySet()) {
				String name = getBeanPropertyStringValue(item);
				c3p0Ds.remove(name);
				BeanDefinition readDsBean = listableBeanFactory.getBeanDefinition(name);
				if (readDsBean != null) {
					beans.add(readDsBean);
				}
			}
		}

		return beans;
	}

	private BeanDefinition getDpdlWriteDsBean(BeanDefinition dataSourceDefinition) {
		String writeDsBeanName = getBeanPropertyStringValue(
				dataSourceDefinition.getPropertyValues().getPropertyValue("writeDS").getValue());

		c3p0Ds.remove(writeDsBeanName);
		BeanDefinition writeDsBean = listableBeanFactory.getBeanDefinition(writeDsBeanName);

		return writeDsBean;
	}

	private Set<String> getGroupDataSourceIgnoreProperties() {
		Set<String> result = new HashSet<String>();
		result.add("jdbcUrl");
		result.add("password");
		result.add("user");
		result.add("driverClass");
		return result;
	}

	private void getJdbcUrlInfo(DataSourceInfo info, String url) {
		if (url != null) {
			info.setUrl(url);

			Matcher m = pattern.matcher(url);
			if (m.find()) {
				info.setType(m.group(1).toLowerCase());
				info.setDatabase(m.group(3).toLowerCase());
			}
		}
	}

	@Override
	public int getOrder() {
		// 必须在DataSourceAutoMonitor之前执行
		return Ordered.LOWEST_PRECEDENCE - 2;
	}

	private RouterType getRouterTypeFromBeans(BeanDefinition mainBean, List<BeanDefinition> beans) {
		boolean hasRead = false;
		boolean hasWrite = false;

		if (mainBean != null) {
			Boolean mainResult = checkBeanIsRead(mainBean);
			if (mainResult != null) {
				if (mainResult.booleanValue()) {
					hasRead = true;
				} else {
					hasWrite = true;
				}
			}
		}

		if (beans != null) {
			for (BeanDefinition bean : beans) {
				Boolean result = checkBeanIsRead(bean);
				if (result != null) {
					if (result.booleanValue()) {
						hasRead = true;
					} else {
						hasWrite = true;
					}
				}
			}
		}

		if (hasRead && !hasWrite) {
			return RouterType.LOAD_BALANCE;
		} else if (hasWrite && !hasRead) {
			return RouterType.FAIL_OVER;
		} else {
			return RouterType.ROUND_ROBIN;
		}
	}

	private String parseUrlExtra(String url) {
		int index = url.lastIndexOf("?");
		if (index >= 0 && index < url.length() - 2) {
			return url.substring(index + 1);
		}
		return null;
	}

	@Override
	public synchronized void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (hasProcessd) {
			return;
		}
		hasProcessd = true;

		listableBeanFactory = (DefaultListableBeanFactory) beanFactory;
		String[] beanDefinitionNames = listableBeanFactory.getBeanDefinitionNames();

		for (String beanDefinitionName : beanDefinitionNames) {
			try {
				AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) listableBeanFactory
						.getBeanDefinition(beanDefinitionName);
				Class<?> beanClazz = beanDefinition.resolveBeanClass(ClassUtils.getDefaultClassLoader());
				if (beanClazz != null && DataSource.class.isAssignableFrom(beanClazz)) {
					if (beanClazz.getName().equals(C3P0_CLASS_NAME)) {
						c3p0Ds.add(beanDefinitionName);
					} else if (beanClazz.getName().equals(DPDL_CLASS_NAME)) {
						dpdlDs.add(beanDefinitionName);
					} else if (beanClazz.getName().equals(GROUP_CLASS_NAME)) {
						groupDs.add(beanDefinitionName);
					} else if (beanClazz.getName().equals(SHARD_CLASS_NAME)) {
						shardDs.add(beanDefinitionName);
					} else {
						otherDs.add(beanDefinitionName);
					}
				}
			} catch (ClassNotFoundException e) {
			}
		}

		replace();
	}

	private void processC3P0() {
		new DataSourceProcesser().process(c3p0Ds, new DataSourceProcesserTemplate() {
			@Override
			public void process(BeanDefinition dataSourceDefinition, String beanName, DataSourceInfo info) {
				Set<PropertyValue> properties = getC3P0PropertyValues(dataSourceDefinition, info,
						getRouterTypeFromBeans(dataSourceDefinition, null));

				if (properties.size() > 0) {
					setGroupDataSourceProperties(dataSourceDefinition, info, properties);

					Cat.logEvent("DAL.BeanFactory", String.format("Replace-%s", beanName));
				} else {
					Cat.logEvent("DAL.BeanFactory", String.format("IgnoreC3P0-%s", beanName));
				}
			}
		});
	}

	private void processDpdl() {
		new DataSourceProcesser().process(dpdlDs, new DataSourceProcesserTemplate() {
			@Override
			public void process(BeanDefinition dataSourceDefinition, String beanName, DataSourceInfo info) {
				BeanDefinition dpdlWriteDsBean = getDpdlWriteDsBean(dataSourceDefinition);
				List<BeanDefinition> dpdlReadDsBean = getDpdlReadDsBean(dataSourceDefinition);

				Set<PropertyValue> properties = getC3P0PropertyValues(dpdlWriteDsBean, info,
						getRouterTypeFromBeans(dpdlWriteDsBean, dpdlReadDsBean));

				if (properties.size() > 0) {
					setGroupDataSourceProperties(dataSourceDefinition, info, properties);

					Cat.logEvent("DAL.BeanFactory", String.format("Replace-%s", beanName));
				} else {
					Cat.logEvent("DAL.BeanFactory", String.format("IgnoreDpdl-%s", beanName));
				}
			}
		});
	}

	private void processGroupDs() {
		new DataSourceProcesser().process(groupDs, new DataSourceProcesserTemplate() {
			@Override
			public void process(BeanDefinition dataSourceDefinition, String beanName, DataSourceInfo info) {
				String jdbcRef;
				PropertyValue propertyValue = dataSourceDefinition.getPropertyValues().getPropertyValue("jdbcRef");

				Object tempValue;
				if (propertyValue == null) {
					ConstructorArgumentValues.ValueHolder valueHolder = (ValueHolder) dataSourceDefinition
							.getConstructorArgumentValues().getGenericArgumentValues().get(0);
					tempValue = valueHolder.getValue();
				} else {
					tempValue = propertyValue.getValue();
				}

				jdbcRef = getBeanPropertyStringValue(tempValue);

				if (StringUtils.isNotBlank(jdbcRef)) {
					if (LionUtil.isLionKey(jdbcRef)) {
						jdbcRef = LionUtil.getLionConfig(LionUtil.trimLionKey(jdbcRef));
					}
					if (StringUtils.isNotBlank(jdbcRef)) {
						// jdbcRef is Url for GroupDataSource
						info.setUrl(jdbcRef);

						DataSourceConfigManager manager = DataSourceConfigManagerFactory
								.getConfigManager(Constants.CONFIG_MANAGER_TYPE_REMOTE, jdbcRef);
						GroupDataSourceConfig config = manager.getGroupDataSourceConfig();
						if (config.getDataSourceConfigs().size() > 0) {
							DataSourceConfig dsConfig = config.getDataSourceConfigs().values().iterator().next();

							if (dsConfig.getJdbcUrl() != null) {
								Matcher m = pattern.matcher(dsConfig.getJdbcUrl());
								if (m.find()) {
									info.setType(m.group(1).toLowerCase());
									info.setDatabase(m.group(3).toLowerCase());
								}
							}
						}

						manager.close();
					}
				}
			}
		});
	}

	private void processShardDs() {
		new DataSourceProcesser().process(shardDs, new DataSourceProcesserTemplate() {
			@Override
			public void process(BeanDefinition dataSourceDefinition, String beanName, DataSourceInfo info) {
				PropertyValue propertyValue = dataSourceDefinition.getPropertyValues().getPropertyValue("ruleName");

				Object tempValue = propertyValue.getValue();
				// ruleName is Url for ShardDataSource
				info.setUrl(getBeanPropertyStringValue(tempValue));
			}
		});
	}

	private void processOther() {
		new DataSourceProcesser().process(otherDs, new DataSourceProcesserTemplate() {
			@Override
			public void process(BeanDefinition dataSourceDefinition, String beanName, DataSourceInfo info) {
				Cat.logEvent("DAL.BeanFactory", String.format("IgnoreOther-%s", beanName));
			}
		});
	}

	private void replace() {
		processDpdl();
		processC3P0();
		processGroupDs();
		processShardDs();
		processOther();

		String url = LionUtil.getLionConfig(UPLOAD_DS_INFO_KEY);
		if (StringUtils.isBlank(url)) {
			Exception exp = new ZebraConfigException(UPLOAD_DS_INFO_KEY + " not exists!");
			logger.warn(exp);
		} else {
			uploadDataSourceInfo(url, infos);
		}
	}

	private void setGroupDataSourceProperties(BeanDefinition dataSourceDefinition, DataSourceInfo info,
			Set<PropertyValue> properties) {
		dataSourceDefinition.setBeanClassName(GroupDataSource.class.getName());
		for (PropertyValue p : dataSourceDefinition.getPropertyValues().getPropertyValues()) {
			dataSourceDefinition.getPropertyValues().removePropertyValue(p);
		}
		for (PropertyValue entity : properties) {
			dataSourceDefinition.getPropertyValues().addPropertyValue(entity.getName(), entity.getValue());
		}
		if (dataSourceDefinition instanceof AbstractBeanDefinition) {
			((AbstractBeanDefinition) dataSourceDefinition).setInitMethodName("init");
		}
		info.setReplaced(true);
	}

	protected void uploadDataSourceInfo(String url, List<DataSourceInfo> infos) {
		try {
			Gson gson = new Gson();

			PrintWriter out = null;
			BufferedReader in = null;
			try {
				String rawData = "infos=" + gson.toJson(infos);

				URL realUrl = new URL(url);
				URLConnection conn = realUrl.openConnection();
				conn.setRequestProperty("accept", "*/*");
				conn.setRequestProperty("connection", "Keep-Alive");
				conn.setConnectTimeout(1000);
				conn.setReadTimeout(5000);
				conn.setDoOutput(true);
				conn.setDoInput(true);

				out = new PrintWriter(conn.getOutputStream());
				out.print(rawData);
				out.flush();
				in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				in.readLine();
			} catch (Exception e) {
			} finally {
				try {
					if (out != null) {
						out.close();
					}
					if (in != null) {
						in.close();
					}
				} catch (Exception ex) {
				}
			}
		} catch (Exception ignore) {
		}
	}

	interface DataSourceProcesserTemplate {
		void process(BeanDefinition bean, String beanName, DataSourceInfo info);
	}

	class DataSourceProcesser {
		public void process(Set<String> ds, DataSourceProcesserTemplate template) {
			for (String beanName : ds) {
				BeanDefinition dataSourceDefinition = listableBeanFactory.getBeanDefinition(beanName);
				DataSourceInfo info = new DataSourceInfo(beanName);
				info.setDataSourceBeanClass(dataSourceDefinition.getBeanClassName());

				try {
					template.process(dataSourceDefinition, beanName, info);
				} catch (Exception ignore) {
				}

				infos.add(info);
			}
		}
	}
}
