package com.dianping.zebra.admin.admin.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DatabaseRealtimeServiceImpl implements DatabaseRealtimeService, Initializable, Task {

	private final String URL_DB = "http://tools.dba.dp/get_useiplist_of_db.php?dbname=%s";

	private final String URL_ALL = "http://tools.dba.dp/get_useiplist_of_db.php?type=all";

	private volatile boolean shutdown = false;

	@Inject
	private CmdbService m_cmdbService;

	@Inject
	private HttpService m_httpService;

	private Map<String, Map<String, String>> m_allConnectedIps = new ConcurrentHashMap<String, Map<String, String>>();;

	@Override
	public Map<String, Map<String, String>> getAllConnectedIps() {
		return m_allConnectedIps;
	}

	private Map<String, String> getAllIpNameMappings(JsonObject obj) {
		JsonObject databases = obj.get("info").getAsJsonObject();

		Set<String> ips = new HashSet<String>();
		for (Entry<String, JsonElement> entry : databases.entrySet()) {
			JsonArray ipArray = entry.getValue().getAsJsonArray();
			for (int i = 0; i < ipArray.size(); i++) {
				String ip = ipArray.get(i).getAsString();

				if (ip.startsWith("10.1") || ip.startsWith("10.2")) {
					ips.add(ip);
				}
			}
		}

		List<String> ipList = new ArrayList<String>();

		for (String ip : ips) {
			ipList.add(ip);
		}

		return m_cmdbService.getMultiAppName(ipList);
	}

	@Override
	public Map<String, String> getConnectedIps(String database) {
		String dbName = database.toLowerCase();
		String url = String.format(URL_DB, dbName);
		try {
			String result = m_httpService.sendGet(url);

			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(result).getAsJsonObject();

			int status = obj.get("status").getAsInt();
			if (status == 0) {
				List<String> ips = new ArrayList<String>();

				JsonArray ipArray = obj.get("info").getAsJsonObject().get(dbName).getAsJsonArray();
				for (int i = 0; i < ipArray.size(); i++) {
					String ip = ipArray.get(i).getAsString();

					if (ip.startsWith("10.1") || ip.startsWith("10.2")) {
						ips.add(ip);
					}
				}

				return m_cmdbService.getMultiAppName(ips);
			} else {
				throw new IOException(obj.get("message").getAsString());
			}
		} catch (Exception e) {
			Cat.logError(e);
			return null;
		}
	}

	@Override
	public String getName() {
		return "Dal-IP-Name-Thread";
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			String content = m_httpService.sendGet(URL_ALL);

			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(content).getAsJsonObject();

			int status = obj.get("status").getAsInt();
			if (status == 0) {
				Map<String, String> ipMappings = getAllIpNameMappings(obj);

				Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
				JsonObject databases = obj.get("info").getAsJsonObject();

				for (Entry<String, JsonElement> entry : databases.entrySet()) {
					JsonArray ipArray = entry.getValue().getAsJsonArray();
					Map<String, String> databaseMaping = new HashMap<String, String>();
					for (int i = 0; i < ipArray.size(); i++) {
						String ip = ipArray.get(i).getAsString();

						if (ip.startsWith("10.1") || ip.startsWith("10.2")) {
							String name = ipMappings.get(ip);

							if (name != null && name.length() > 0) {
								databaseMaping.put(ip, name);
							}
						}
					}

					System.out.println("database begin : " + entry.getKey());
					result.put(entry.getKey(), databaseMaping);
					System.out.println("database end : " + entry.getKey());
				}

				m_allConnectedIps = result;
			} else {
				throw new IOException(obj.get("message").getAsString());
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	@Override
	public void run() {
		while (!shutdown) {
			try {
				initialize();
			} catch (Throwable ignore) {
				Cat.logError(ignore);
			}

			try {
				TimeUnit.HOURS.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void shutdown() {
		shutdown = true;
	}
}