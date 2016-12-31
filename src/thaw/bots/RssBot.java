package thaw.bots;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import io.vertx.core.json.JsonObject;
 
public class RssBot implements Callable<JsonObject>{
	
	private final URL url;
	private final HttpURLConnection httpcon;
	private final SyndFeedInput input = new SyndFeedInput();
	private final SyndFeed feed;
	private	List<SyndFeed> entries;
	private Iterator<SyndFeed> itEntries;
	private final Object lock = new Object();
	
		/*
		 * RSS Bot constructor
		 * 
		 * @param String url
		 */
		@SuppressWarnings("unchecked")
		public RssBot(String str_url) throws IOException, IllegalArgumentException, FeedException{
			synchronized(lock){
				this.url = new URL(str_url);
				this.httpcon = (HttpURLConnection)url.openConnection();
				this.feed = input.build(new XmlReader(httpcon));
				this.entries = feed.getEntries();
				this.itEntries = entries.iterator();
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.concurrent.Callable#call()
		 * retrieve feed informations and return it as jsonObject
		 * 
		 * @return JsonObject
		 */
		@Override
		public JsonObject call(){
			synchronized(lock){
				JsonObject json = new JsonObject();
				StringBuilder builder = new StringBuilder();
				while (itEntries.hasNext()) {
					SyndEntry entry = (SyndEntry) itEntries.next();
					builder.append("<div><p>Title: " + entry.getTitle()).append("</p><p>Link: " + entry.getLink())
						.append("</p><p>Author: " + entry.getAuthor())
						.append("</p><p>Publish Date: " + entry.getPublishedDate())
						.append("</p><p>Description: " + entry.getDescription().getValue())
						.append("</p></div></br>");
				}
				json.put("content",builder.toString());
				json.put("username",BotsHandler.RSS_BOT);
				return json;
			}
		}

 
		
		
	
}