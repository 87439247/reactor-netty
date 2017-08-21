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

package reactor.ipc.netty.tcp;

import java.util.Objects;
import java.util.function.Consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * @author Stephane Maldini
 */
final class TcpClientSecure extends TcpClientOperator {

	final SslProvider sslProvider;

	TcpClientSecure(TcpClient client, Consumer<? super SslProvider.SslContextSpec> sslProviderBuilder) {
		super(client);
		Objects.requireNonNull(sslProviderBuilder, "sslProviderBuilder");

		SslProvider.Build builder = (SslProvider.Build) SslProvider.builder();
		sslProviderBuilder.accept(builder);
		this.sslProvider = builder.build();
	}

	@Override
	public Bootstrap configure() {
		return TcpUtils.updateSslSupport(source.configure(), sslProvider);
	}

	static final SslContext DEFAULT_SSL_CONTEXT;

	static {
		SslContext sslContext;
		try {
			sslContext = SslContextBuilder.forClient()
			                              .build();
		}
		catch (Exception e) {
			sslContext = null;
		}
		DEFAULT_SSL_CONTEXT = sslContext;
	}


	@Override
	public SslContext sslContext(){
		return this.sslProvider.getSslContext();
	}
}
