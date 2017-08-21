/*
 * Copyright (c) 2011-2017 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package reactor.ipc.netty.http.server;

import java.util.Objects;
import java.util.function.Function;

import reactor.ipc.netty.tcp.TcpServer;

/**
 * @author Stephane Maldini
 */
final class HttpServerTcpConfig extends HttpServerOperator {

	final TcpServer server;

	HttpServerTcpConfig(HttpServer server,
			Function<? super TcpServer, ? extends TcpServer> bootstrapMapper) {
		super(server);
		Objects.requireNonNull(bootstrapMapper, "tcpMapper");
		this.server = Objects.requireNonNull(bootstrapMapper.apply(source.tcpConfiguration()),
				"tcpMapper");
	}

	@Override
	protected TcpServer tcpConfiguration() {
		return server;
	}
}
