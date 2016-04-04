/*
 * Copyright 2015 Comcast Cable Communications Management, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.comcast.cdn.traffic_control.traffic_router.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.comcast.cdn.traffic_control.traffic_router.core.cache.CacheRegister;
import com.comcast.cdn.traffic_control.traffic_router.core.router.TrafficRouter;
import com.comcast.cdn.traffic_control.traffic_router.core.router.TrafficRouterManager;

public class LanguidState {
	private static final Logger LOGGER = Logger.getLogger(LanguidState.class);
	private boolean ready = false;
	private TrafficRouterManager trafficRouterManager;
	private int port = 0;
	private int apiPort = 0;

	public void init() {
		if (trafficRouterManager == null || trafficRouterManager.getTrafficRouter() == null) {
			setReady(true);
			return;
		}

		final TrafficRouter tr = trafficRouterManager.getTrafficRouter();

		if (tr.getCacheRegister() == null) {
			setReady(true);
			return;
		}

		final CacheRegister cacheRegister = tr.getCacheRegister();
		final JSONObject routers = cacheRegister.getTrafficRouters();

		try {
			final String hostname = InetAddress.getLocalHost().getHostName().replaceAll("\\..*", "");

			for (String key : JSONObject.getNames(routers)) {
				final JSONObject routerJson = routers.optJSONObject(key);

				if (hostname.equalsIgnoreCase(key)) { // this is us
					if (routerJson.has("port")) {
						this.setPort(routerJson.optInt("port"));
					}

					if (routerJson.has("api.port")) {
						this.setApiPort(routerJson.optInt("api.port"));
					}

					break;
				}
			}
		} catch (UnknownHostException e) {
			LOGGER.error(e, e);
		}

		this.setReady(true);
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(final boolean ready) {
		this.ready = ready;
	}

	public int getPort() {
		return port;
	}

	public void setPort(final int port) {
		this.port = port;
	}

	public int getApiPort() {
		return apiPort;
	}

	public void setApiPort(final int apiPort) {
		this.apiPort = apiPort;
	}

	public TrafficRouterManager getTrafficRouterManager() {
		return trafficRouterManager;
	}

	public void setTrafficRouterManager(final TrafficRouterManager trafficRouterManager) {
		this.trafficRouterManager = trafficRouterManager;
	}
}
