import io.undertow.Undertow;
import io.undertow.util.Headers;
import io.undertow.Handlers;

import java.util.ArrayDeque;


public class Main {
    public static void main(String[] args) {
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(Handlers.path()
                        .addExactPath("helloworld",(exchange)->{
                            String name = exchange.getQueryParameters().getOrDefault("name", new ArrayDeque<String>()).poll();
                         exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                         exchange.getResponseSender().send("Hello World by" + name);
                        })
                        .addPrefixPath("/", exchange -> {
                            String name = exchange.getQueryParameters().getOrDefault("name", new ArrayDeque<String>()).poll();
                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                            exchange.getResponseSender().send("Hello, " + name);
                        })
                ).build();
        server.start();
    }
}
