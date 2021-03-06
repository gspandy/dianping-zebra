package com.dianping.zebra.monitor.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;

import com.dianping.cat.Cat;
import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;

/**
 * Created by Dozer on 8/13/14.
 */
public final class LionUtil {
	public static String getLionValueFromBean(BeanDefinition bean, String key) {
		try {
			String value = ((TypedStringValue) bean.getPropertyValues().getPropertyValue(key).getValue()).getValue()
					.trim();

			return isLionKey(value) ? getLionConfig(trimLionKey(value)) : value;
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean isLionKey(String key) {
		return key.startsWith("${") && key.endsWith("}");
	}

	public static String trimLionKey(String key) {
		return isLionKey(key) ? key.substring(2, key.length() - 1) : key;
	}

	public static String getLionConfig(String key) {
		try {
			String property = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getProperty(key);

			if (property == null) {
				Cat.logEvent("DAL.NullValueLionKey", key);
			}
			return property;
		} catch (LionException e) {
			return null;
		}
	}
}
