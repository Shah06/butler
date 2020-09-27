# Butler
##### *1.0-SNAPSHOT*

Butler is a pseudo-http server used for writing full-stack web applications in Java, with NodeJS inspired syntax.

### Roadmap
1. Add SQL API
2. Static file caching
3. Less code to get started (perhaps have one single instantiator class)

### Example class
The following is a basic, yet full featured web application. It sets up a file server at a directory, keeps track of page visits, and has a database-esque application which keeps an array of names entered through a form (via POST).


Note that there is a lot of boilerplate code, such as imports, and configuring properties. For now, copy this into your application for ease of use. This will be addressed in a later patch.


```
package org.atharvashah.butler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.atharvashah.butler.utils.properties.PropertiesManager;
import org.atharvashah.butler.utils.requests.RequestHandlerManager;

/**
 * Hello world!
 */
public final class App {

    static int pageVisit = 0;
    static ArrayList<String> names = new ArrayList<String>();
    static String PUBLIC_FILEPATH = PropertiesManager.get("public_filepath");
    static {
        if (null == PUBLIC_FILEPATH) PUBLIC_FILEPATH = "public/";
    }

    private App() {
    }

    /**
     * Example "application"
     */
    public static void main(String[] args) {

        RequestHandlerManager manager = RequestHandlerManager.getInstance();

        manager.get("*", (req, res) -> {
            try {
                File f = new File(PUBLIC_FILEPATH + req.path);
                if (f.exists()) {
                    res.write("HTTP/1.1 200 OK\nContent-Type: text/html\n\n");
                    res.write(FileUtils.readFileToByteArray(f));
                } else {
                    res.write("HTTP/1.1 404 Not Found\n\n");
                    res.write("<!DOCTYPE html><h1>404 Not Found</h1>");
                    res.write("<p>The requested URL <code>" + req.path + "</code> was not found on the server.</p>");
                }
            } catch (IOException e) {}
            // res.write("Requested: " + new File(req.path).getAbsolutePath());
        });

        manager.post("/form.html", (req, res) -> {
            res.write("HTTP/1.1 200 OK\nContent-Type: text/html\n\n");
            res.write("<h3>List of submitted names:</h3><ul>");

            String name = req.requestBody;
            String fname = name.split("&")[0].split("=")[1];
            String lname = name.split("&")[1].split("=")[1];

            names.add("<li>" + fname + " " + lname + "</li>");
            names.forEach((e) -> res.write(e));
            
            res.write("</ul>");
            
        });

        manager.get("/", (req, res) -> {
            File f = new File("logs/app.log");
            try {
                res.write("HTTP/1.1 200 OK\n\n");
                InputStream is = new FileInputStream(f);
                byte[] data = new byte[is.available()];
                is.read(data);
                res.write(data);
                is.close();
            } catch (FileNotFoundException e) {
                res.write("HTTP/1.1 404 Not Found");
            } catch (Exception e) {
                res.write("HTTP/1.1 500 Internal Server Error");
            }
        });

        manager.get("/clear", (req, res) -> {
            try {
                BufferedWriter writer = Files.newBufferedWriter(Paths.get("logs/app.log"));
                writer.write("");
                writer.flush();
                res.write("HTTP/1.1 200 OK\nContent-Type: text/html\n\n<h1>Test HTML</h1>");
                res.write("<p>Cleared app.log</p>");
            } catch (IOException e){};
        });

        manager.get("/app", (req, res) -> {
            res.write("HTTP/1.1 200 OK\nContent-Type: text/html\n\n<h1>Test App</h1>");
            res.write("<p>PageVisits: " + (++pageVisit) + "</p>");
        });

        manager.post("/post_endpoint", (req, res) -> {
            res.write("HTTP/1.1 200 OK\nContent-Type:text/plain\n\n");
            // System.out.println(req.request);
            res.write(req.request);
        });

        // start up a new HttpServer
        new HttpServer();

    }
}
```