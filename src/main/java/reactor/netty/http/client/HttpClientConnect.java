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

package reactor.netty.http.client;

import java.util.Objects;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.JdkSslContext;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.http.HttpResources;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpClient;

/**
 * @author Stephane Maldini
 */
final class HttpClientConnect extends HttpClient {

	static final HttpClientConnect INSTANCE = new HttpClientConnect();

	final TcpClient tcpClient;

	HttpClientConnect() {
		this(DEFAULT_TCP_CLIENT);
	}

	HttpClientConnect(TcpClient tcpClient) {
		this.tcpClient = Objects.requireNonNull(tcpClient, "tcpClient");
	}

	@Override
	protected Mono<? extends Connection> connect(Bootstrap b) {
		if (b.config()
		     .group() == null) {
			LoopResources loops = HttpResources.get();

			boolean useNative = LoopResources.DEFAULT_NATIVE && !(tcpClient.sslContext() instanceof JdkSslContext);

			EventLoopGroup elg = loops.onClient(useNative);

			b.group(elg)
			 .channel(loops.onChannel(elg));
		}
		return tcpClient.connect(b);
	}

	@Override
	protected TcpClient tcpConfiguration() {
		return tcpClient;
	}
}
