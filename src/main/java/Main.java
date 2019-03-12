import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.undertow.Undertow;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Headers;
import io.undertow.Handlers;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;


public class Main {
    public static void main(String[] args) throws IOException {

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setClassLoaderForTemplateLoading(Main.class.getClassLoader(),"template");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);

        final Template nameTemplate = cfg.getTemplate("name.html");

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(Handlers.path()
                        .addExactPath("helloworld",(exchange)->{
                         exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                         exchange.getResponseSender().send("Hello World");
                        })
                        .addExactPath("form",new EagerFormParsingHandler().setNext((exchange)->{
                            FormData form = exchange.getAttachment(FormDataParser.FORM_DATA);
                            FormData.FormValue firstFv = form.getFirst("name");
                            String name = firstFv.getValue();

                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");

                            HashMap<String, Object> templateData = new HashMap<String, Object>();
                            templateData.put("name", name);
                            StringWriter stringWriter = new StringWriter();
                            nameTemplate.process(templateData, stringWriter);

                            exchange.getResponseSender().send(stringWriter.toString());
                        }))
                        .addPrefixPath("/",new ResourceHandler(new ClassPathResourceManager(Main.class.getClassLoader(), "html")))
                ).build();
        server.start();
    }
}
