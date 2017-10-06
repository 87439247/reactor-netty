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

package reactor.netty.udp;

import java.util.function.Consumer;

import io.netty.bootstrap.Bootstrap;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

/**
 * @author Stephane Maldini
 */
final class UdpClientLifecycle extends UdpClientOperator implements Consumer<Connection> {

	final Consumer<? super Bootstrap> onBind;
	final Consumer<? super Connection>      onBound;
	final Consumer<? super Connection>      onUnbound;

	UdpClientLifecycle(UdpClient server,
			Consumer<? super Bootstrap> onBind,
			Consumer<? super Connection> onBound,
			Consumer<? super Connection> onUnbound) {
		super(server);
		this.onBind = onBind;
		this.onBound = onBound;
		this.onUnbound = onUnbound;
	}

	@Override
	protected Mono<? extends Connection> bind(Bootstrap b) {
		Mono<? extends Connection> m = source.bind(b);

		if (onBind != null) {
			m = m.doOnSubscribe(s -> onBind.accept(b));
		}

		if (onBound != null) {
			m = m.doOnNext(this);
		}

		if (onUnbound != null) {
			m = m.doOnNext(c -> c.onDispose(() -> onUnbound.accept(c)));
		}

		return m;
	}

	@Override
	public void accept(Connection o) {
		onBound.accept(o);
	}
}
