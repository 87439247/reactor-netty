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
package reactor.netty.channel;

import java.time.Duration;
import java.util.Random;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;

public class FluxReceiveTest {

	@Test
	public void testByteBufsReleasedWhenTimeout() {
		ResourceLeakDetector.setLevel(Level.PARANOID);

		byte[] content = new byte[1024*8];
		Random rndm = new Random();
		rndm.nextBytes(content);

		DisposableServer server1 =
				HttpServer.create()
				          .tcpConfiguration(tcp -> tcp.port(0))
				          .router(routes ->
						          routes.get("/target", (req, res) ->
								          res.sendByteArray(Flux.just(content)
								                                .delayElements(Duration.ofMillis(100)))))
				          .bindNow();

		DisposableServer server2 =
				HttpServer.create()
				          .tcpConfiguration(tcp -> tcp.port(0))
				          .router(routes ->
						          routes.get("/forward", (req, res) ->
								          HttpClient.create("/target")
								                    .tcpConfiguration(tcp -> tcp.port(server1.port()))
								                    .get()
								                    .responseSingle((r, body) -> body.asString())
								                    .log()
								                    .delayElement(Duration.ofMillis(50))
								                    .timeout(Duration.ofMillis(50))
								                    .then()
						          )
				          )
				          .bindNow();

		Flux.range(0, 50)
		    .flatMap(i -> HttpClient.create("/forward")
		                            .tcpConfiguration(tcp -> tcp.port(server2.port()))
		                            .get()
		                            .response()
		                            .log())
		    .blockLast(Duration.ofSeconds(30));

		ResourceLeakDetector.setLevel(Level.SIMPLE);
	}
}