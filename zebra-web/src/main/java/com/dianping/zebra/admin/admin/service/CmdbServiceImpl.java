package com.dianping.zebra.admin.admin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.zebra.group.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CmdbServiceImpl implements CmdbService {

	private final String URL = "http://api.cmdb.dp/api/v0.1/ip/%s/projects";

	private final String URL_BATCH = "http://api.cmdb.dp/api/v0.1/projects/by_ips";

	private final String QUOTE = "\"\"";

	@Inject
	private HttpService httpService;

	@Override
	public String getAppName(String ip) {
		String url = String.format(URL, ip);

		Transaction transaction = Cat.newTransaction("Cmdb", "single");

		try {
			String result = httpService.sendGet(url);
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(result).getAsJsonObject();
			JsonArray array = obj.get("projects").getAsJsonArray();
			if (array.size() > 0) {
				String name = array.get(0).getAsJsonObject().get("project_name").getAsString();

				return name;
			}

			transaction.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			Cat.logError(e);
			transaction.setStatus(e);
		} finally {
			transaction.complete();
		}

		return Constants.PHOENIX_APP_NO_NAME;
	}

	public Map<String, String> getMultiAppName(List<String> ips) {
		Transaction transaction = Cat.newTransaction("Cmdb", "mutiple");

		Map<String, String> ipNames = new HashMap<String, String>();
		try {
			StringBuilder sb = new StringBuilder(1024);
			boolean isFirst = true;
			for (String ip : ips) {
				if (isFirst) {
					sb.append(ip);
					isFirst = false;
				} else {
					sb.append(",");
					sb.append(ip);
				}
			}

			String content = httpService.sendPost(URL_BATCH, "ip=" + sb.toString());

			JsonParser parser = new JsonParser();
			for (Entry<String, JsonElement> entry : parser.parse(content).getAsJsonObject().entrySet()) {
				JsonElement value = entry.getValue();
				if (!value.toString().equals(QUOTE)) {
					String ip = entry.getKey();
					String name = value.getAsJsonObject().get("project_name").getAsString();

					ipNames.put(ip, name);
				}
			}

			transaction.setStatus(Message.SUCCESS);
		} finally {
			transaction.complete();
		}

		return ipNames;
	}
}
