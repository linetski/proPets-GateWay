package propets.gateway;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleFilter extends ZuulFilter {

	  private static Logger log = LoggerFactory.getLogger(SimpleFilter.class);
	  private static Date lastUpdated;
	  static{
		  lastUpdated = new Date();
	  }

	  @Override
	  public String filterType() {
	    return "pre";
	  }

	  @Override
	  public int filterOrder() {
	    return 1;
	  }

	  @Override
	  public boolean shouldFilter() {
	    return true;
	  }

	  @Override
	  public Object run() {
	    RequestContext ctx = RequestContext.getCurrentContext();
	    HttpServletRequest request = ctx.getRequest();
	    
	    Date date = new Date();
	    long diff = date.getTime() - lastUpdated.getTime();
	    long minutes = TimeUnit.MILLISECONDS.toMinutes(diff); 
	    if(minutes > 25) {
	    	log.info("wakeup services start");
		    triggerUrl("https://propets-eurekaservice.herokuapp.com");
		    triggerUrl("https://propets-configuration-service.herokuapp.com");
		    triggerUrl("https://propets-auth-service.herokuapp.com");
		    triggerUrl("https://propets-lostandfoundservice.herokuapp.com");
		    triggerUrl("https://propets-elastic-service.herokuapp.com");
		    triggerUrl("https://propets-notification-service.herokuapp.com");
		    log.info("wakeup services end");
	    }
	    lastUpdated = date;
	    log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

	    return null;
	  }
	  
	  public void triggerUrl(String urlString) {
		  try {
				 // Create a neat value object to hold the URL
				    URL url = new URL(urlString);
				 // Open a connection(?) on the URL(??) and cast the response(???)
				    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				    // This line makes the request
				    connection.getInputStream();
				    } catch(Exception e) {
				    	
				    }
	  }

	}