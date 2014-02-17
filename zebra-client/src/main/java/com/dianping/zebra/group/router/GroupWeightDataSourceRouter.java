package com.dianping.zebra.group.router;import java.util.List;import java.util.Random;import java.util.TreeSet;import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;/** *  *  *  * 对等数据库管理器 可以是读对等：如多个读库，每个库的数据完全相同。对等读取 可以是写对等：如日志库，每个库数据不同，一条数据写入哪个库都可以。对等写入 *  * 支持动态推送权重，动态加减库。 *  */public class GroupWeightDataSourceRouter extends AbstractGroupDataSourceRouter {	// private static final Logger logger = LoggerFactory.getLogger(WeightDataSourcerouter.class);	private boolean readOnly;	private WeightRandom weightRandom;	private List<DataSourceConfig> DataSourceConfigs;	public GroupWeightDataSourceRouter(List<DataSourceConfig> DataSourceConfigs, boolean isReadOnly) {		this.DataSourceConfigs = DataSourceConfigs;		this.readOnly = isReadOnly;		this.weightRandom = new WeightRandom(DataSourceConfigs);	}	@Override	public GroupDataSourceTarget select(GroupDataSourceRouterInfo routerInfo) {		String dataSourceId = weightRandom.select();		return new GroupDataSourceTarget(dataSourceId, readOnly);	}	public boolean isReadOnly() {		return readOnly;	}	public void setReadOnly(boolean isReadOnly) {		this.readOnly = isReadOnly;	}	public List<DataSourceConfig> getDataSourceConfigs() {		return DataSourceConfigs;	}	public void setDataSourceConfigs(List<DataSourceConfig> DataSourceConfigs) {		this.DataSourceConfigs = DataSourceConfigs;	}	private static class WeightRandom {		private int weightAreaSize = 0;		private TreeSet<WeightArea> weights = new TreeSet<WeightArea>();		private Random random = new Random();		public WeightRandom(List<DataSourceConfig> DataSourceConfigs) {			// check dataSourceWeights (unique,and weight larger than 0)			check(DataSourceConfigs);			// build area			for (DataSourceConfig DataSourceConfig : DataSourceConfigs) {				weightAreaSize += DataSourceConfig.getWeight();				WeightArea weightArea = new WeightArea(DataSourceConfig.getId(), weightAreaSize - 1);				weights.add(weightArea);			}		}		private void check(List<DataSourceConfig> DataSourceConfigs) {			if (DataSourceConfigs == null || DataSourceConfigs.size() <= 0) {				throw new IllegalArgumentException("DataSourceConfigs should not be empty.");			}		}		public String select() {			int randomNum = random.nextInt(weightAreaSize);			String dataSourceId = weights.ceiling(new WeightArea(null, randomNum)).dataSourceId;			return dataSourceId;		}		private static class WeightArea implements Comparable<WeightArea> {			private String dataSourceId;			private int end;			private WeightArea(String dataSourceId, int end) {				super();				this.dataSourceId = dataSourceId;				this.end = end;			}			@Override			public int compareTo(WeightArea o) {				return end - o.end;			}		}	}	@Override   public String getRouterStrategy() {	   return "weight-random";   }}