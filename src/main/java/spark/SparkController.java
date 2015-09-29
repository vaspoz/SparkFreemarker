package spark;

import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

public class SparkController {
    private Configuration config;


    public SparkController() throws IOException{

        config = createFreemarkerConfiguration();
        Spark.setPort(8787);
        initializeRoutes();

    }


    abstract class FreemarkerRoute extends Route {
        final Template template;


        protected FreemarkerRoute(final String path,
                                  final String templateName)
                throws IOException {
            super(path);
            template = config.getTemplate(templateName);
        }


        @Override
        public Object handle(Request request, Response response) {
            StringWriter writer = new StringWriter();
            try {
                doHandle(request, response, writer);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return writer;
        }


        protected abstract void doHandle(final Request request,
                                         final Response response,
                                         final Writer writer)
                throws IOException, TemplateException;

    }


    private void initializeRoutes() throws IOException{

        Spark.get(new FreemarkerRoute("/", "input-page.html") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                SimpleHash root = new SimpleHash();

                root.put("title", "Input your name, weak user!");

                template.process(root, writer);
            }
        });

        Spark.post(new FreemarkerRoute("/", "input-page.html") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                String user = request.queryParams("userName");

                response.raw().addCookie(new Cookie("name", user));

                response.redirect("/hello");
            }
        });

        Spark.get(new FreemarkerRoute("/hello", "hello-page.html") {
            @Override
            protected void doHandle(Request request, Response response, Writer writer) throws IOException, TemplateException {
                SimpleHash root = new SimpleHash();

                String user = getCookieValue(request, "name");
                if (user == null) {
                    user = "Unknown User";
                }

                root.put("title", "Greetings");
                root.put("userName", user);
                root.put("currentTime", new Date());

                template.process(root, writer);
            }
        });
    }


    private Configuration createFreemarkerConfiguration() {

        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(SparkController.class, "/freemarker");
        return cfg;

    }


    private String getCookieValue(final Request request, final String cookieName) {

        if (request.raw().getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals(cookieName))
                return cookie.getValue();
        }

        return null;

    }


    public static void main(String[] args) throws IOException{

        SparkController spark = new SparkController();

    }

}
