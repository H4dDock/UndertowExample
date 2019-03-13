import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.undertow.Undertow;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Headers;
import io.undertow.Handlers;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;


public class Main {
    private static Deque<String> EMPTY_DEQUE = new ArrayDeque<>(0);

    public static void main(String[] args) throws IOException {

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setClassLoaderForTemplateLoading(Main.class.getClassLoader(), "template");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setObjectWrapper(new BeansWrapper(Configuration.VERSION_2_3_23));

        final Template nameTemplate = cfg.getTemplate("name.html");
        final Template questionTemplate = cfg.getTemplate("question.html");

        List<Question> questions = Arrays.asList(
                new Question(" 2 + 2", Arrays.asList("2", "5", "4"), 2),
                new Question(" 3 + 3", Arrays.asList("6", "8", "9"), 0),
                new Question(" 1 - 1", Arrays.asList("-1", "0", "1"), 1)
        );
        for (int i = 0; i < questions.size(); i++) {
            questions.get(i).setNum(i);
        }

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(Handlers.path()
                        .addExactPath("helloworld", (exchange) -> {
                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                            exchange.getResponseSender().send("Hello World");
                        })
                        .addExactPath("question", (exchange) -> {

                            int q = 0;
                            String qstring = exchange.getQueryParameters().getOrDefault("q", EMPTY_DEQUE).peek();
                            if (qstring != null)
                                q = Integer.parseInt(qstring);

                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");

                            StringWriter stringWriter = new StringWriter();
                            questionTemplate.process(questions.get(q), stringWriter);

                            exchange.getResponseSender().send(stringWriter.toString());
                        })
                        .addExactPath("answer", new EagerFormParsingHandler().setNext((exchange) -> {
                                    FormData form = exchange.getAttachment(FormDataParser.FORM_DATA);
                                    FormData.FormValue qfw = form.getFirst("q");
                                    int q = Integer.parseInt(qfw.getValue());

                                    FormData.FormValue answerFw = form.getFirst("answer");
                                    String answer = answerFw.getValue();

                                    Question question = questions.get(q);
                                    boolean right = question.getAnswers().get(question.right).equals(answer);
                                    System.out.println("answer = " + answer + " right=" + right);

                                    int rightAnswers = 0;
                                    Cookie cookie = exchange.getRequestCookies().get("rightAnswers");
                                    if (cookie != null)
                                        rightAnswers = Integer.parseInt(cookie.getValue());
                                    if (right) rightAnswers++;

                                    if (q < questions.size() - 1) {
                                        exchange.getResponseCookies().put("rightAnswers", new CookieImpl("rightAnswers").setValue(rightAnswers + ""));
                                        Handlers.redirect("question?q=" + (q + 1)).handleRequest(exchange);
                                    }else{
                                        if(cookie != null)
                                            exchange.getResponseCookies().put("rightAnswers", cookie
                                                    .setValue("0")
                                                    .setMaxAge(0)
                                            );
                                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                                        exchange.getResponseSender().send("Right answers: " + rightAnswers + " of "+questions.size());
                                    }
                                }
                        ))
                        .addExactPath("form", new EagerFormParsingHandler().setNext((exchange) -> {

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
                        .addPrefixPath("/", new ResourceHandler(new ClassPathResourceManager(Main.class.getClassLoader(), "html")))
                ).build();
        server.start();
    }
}
