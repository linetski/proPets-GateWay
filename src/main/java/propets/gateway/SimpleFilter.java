package propets.gateway;

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
	
	public class WakeUpHerokuProcessThread extends Thread {
		
		String urlString;
		
		public WakeUpHerokuProcessThread(String urlString) {
			this.urlString = urlString;
		}
		
	    public void run(){
	    	try {
				 // Create a neat value object to hold the URL
				    URL url = new URL(this.urlString);
				 // Open a connection(?) on the URL(??) and cast the response(???)
				    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				    connection.setConnectTimeout(60 * 1000);
				    // This line makes the request
				    connection.getInputStream();
				    log.info(urlString + "wakedUp");
				    } catch(Exception e) {
				    	
				    }
	    }
	  }

	  private static Logger log = LoggerFactory.getLogger(SimpleFilter.class);
	  private static Date lastUpdated;
	  private static boolean firstRun = true;
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
	    log.info("service was idle for " + minutes + "minutes");
	    if(minutes > 25 || firstRun) {
	    	firstRun=false;
	    	log.info("wakeup services start");
	    	WakeUpHerokuProcessThread t1 = new WakeUpHerokuProcessThread("https://propets-eurekaservice.herokuapp.com");
	    	t1.start();	    	
	    	WakeUpHerokuProcessThread t2 = new WakeUpHerokuProcessThread("https://propets-configuration-service.herokuapp.com");
	    	t2.start();	    	
	    	WakeUpHerokuProcessThread t3 = new WakeUpHerokuProcessThread("https://propets-auth-service.herokuapp.com");
	    	t3.start();	    	
	    	WakeUpHerokuProcessThread t4 = new WakeUpHerokuProcessThread("https://propets-lostandfoundservice.herokuapp.com");
	    	t4.start();	    	
	    	WakeUpHerokuProcessThread t5 = new WakeUpHerokuProcessThread("https://propets-elastic-service.herokuapp.com");
	    	t5.start();	    	
	    	WakeUpHerokuProcessThread t6 = new WakeUpHerokuProcessThread("https://propets-notification-service.herokuapp.com");
	    	t6.start();
	    	
	    	try {
				t1.join();
				t2.join();
		    	t3.join();
		    	t4.join();
		    	t5.join();
		    	t6.join();
			} catch (InterruptedException e) {
				log.info("InterruptedException occured");
			}
		    log.info("wakeup services end");
		    lastUpdated = date;
	    }
	    
	    log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

	    return null;
	  }
	  
	  public void triggerUrl(String urlString) {
		  
	  }

	}