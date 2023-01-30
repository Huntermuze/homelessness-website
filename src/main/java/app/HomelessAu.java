package app;

import app.pages.AdvancedOtherFactors;
import app.pages.AdvancedOverTime;
import app.pages.CSVPage;
import app.pages.LearnMore;
import io.javalin.Javalin;
import io.javalin.core.util.RouteOverviewPlugin;
import io.javalin.http.staticfiles.Location;


/**Main Application Class.
 *
 * Running this class as regular java application will start the
 * Javalin HTTP Server and our web application.
 *
 * @author Alexander Mitchell
 * @author Matthew Yamen
 *
 * Adapted from code by Timothy Wiley and Santha Sumanasekara
 */
public class HomelessAu
{
	public static final int JAVALIN_PORT = 7000;

	public static final String CSS_DIR = "css/";
	public static final String CSS_DIR_WEB = "/css";

	public static final String IMAGES_DIR = "images/";
	public static final String IMAGES_DIR_WEB = "/images";

	public static final String JS_DIR = "js/";
	public static final String JS_DIR_WEB = "/js";

	public static final String STATIC_ROOT_DIR = "staticWebRoot/";
	public static final String STATIC_ROOT_DIR_WEB = "/";

	public static void main(String[] args)
	{
		//Create our HTTP server and listen in port 7000
		Javalin app = Javalin.create(config -> {
			config.registerPlugin(new RouteOverviewPlugin("/help/routes"));

			//Files in resources/css will appear in the web server at /css
			config.addStaticFiles(CSS_DIR_WEB, CSS_DIR, Location.CLASSPATH);

			//Files in resources/images will appear in the web server at /images
			config.addStaticFiles(IMAGES_DIR_WEB, IMAGES_DIR, Location.CLASSPATH);

			//Files in resources/js will appear in the web server at /js
			config.addStaticFiles(JS_DIR_WEB, JS_DIR, Location.CLASSPATH);

			//Files in resources/staticWebRoot will appear in the web server at /
			config.addStaticFiles(STATIC_ROOT_DIR_WEB, STATIC_ROOT_DIR, Location.CLASSPATH);
		}).start(JAVALIN_PORT);

		//Configure Web Routes
		configureRoutes(app);

		System.out.println("--------##HOMELESSAU##--------\nBy Huntermuze & VisibleReality");
	}

	private static void configureRoutes(Javalin app)
	{
		AdvancedOtherFactors advancedOtherFactors = new AdvancedOtherFactors("POST");
		AdvancedOverTime advancedOverTime = new AdvancedOverTime("POST");

		app.get("/", (ctx) -> ctx.render("index.html"));
		app.get("/index.html", (ctx) -> ctx.render("index.html"));
		app.get("/learn-more.html", new LearnMore());
		app.get("/advanced-other-factors.html", new AdvancedOtherFactors("GET"));
		app.get("/advanced-over-time.html", new AdvancedOverTime("GET"));

		app.post("/advanced-other-factors.html", advancedOtherFactors);
		app.post("/advanced-other-factors.csv", new CSVPage("AOF", advancedOtherFactors));

		app.post("/advanced-over-time.html", advancedOverTime);
		app.post("/advanced-over-time.csv", new CSVPage("AOT", advancedOverTime));
	}

}
