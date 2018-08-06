package jcvs.org;
/* 
 * if you're wondering about why the name...
 * A fence, also known as a receiver,  is an 
 * individual who knowingly buys stolen goods in order 
 * to later resell them for profit.
 * 
 * 
 */

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;

@ServerEndpoint("/fence")
public class Fence {
	private static Set<Session> clients = 
			Collections.synchronizedSet(new HashSet<Session>());
	
	
	@OnMessage
	public void onMessage (String msg, Session session)
			throws IOException{
		JSONObject json = new JSONObject();
		json.put("status", 200).put("msg", msg);
		
		synchronized (clients) {
			for (Session client : clients) {
					client.getBasicRemote().sendText(json.toString());
			}
		}
		
	}
	
	@OnOpen
	public void onOpen(Session session) throws IOException{
			clients.add(session);
			String[] files = getFiles();
			for(String file : files) {
				System.out.println("file "+file+"\n");
				
			}
	}
	
	@OnClose
	public void onClose (Session session) throws IOException{
		clients.remove(session);
		JSONObject json = new JSONObject();
		json.put("status", 200).put("msg", "Alguien abandono la sala");
		synchronized (clients) {
			for (Session client : clients) {
				client.getBasicRemote().sendText( json.toString());
			}
		}
		
	}
	@OnError
	public void onError(Throwable e) throws IOException {
	    e.printStackTrace();
		JSONObject json = new JSONObject();
		json.put("status", 500).put("msg", e.getStackTrace());
	    synchronized (clients) {
			for (Session client : clients) {
				client.getBasicRemote().sendText( json.toString());
			}
		}
	}
	
	public String[] getFiles() {
		File dir;
		try {
			dir = new File(getCurrentPath());
			Collection<String> files  =new ArrayList<String>();

		    if(dir.isDirectory()){
		        File[] listFiles = dir.listFiles();

		        for(File file : listFiles){
		            if(file.isFile()) {
		                files.add(file.getName());
		            }
		        }
		    }

		    return files.toArray(new String[]{});
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
			// TODO Auto-generated catch block
			
		}
		

	    
	}
	
	public String getCurrentPath() throws URISyntaxException {
		
		CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
		File jarFile = new File(codeSource.getLocation().toURI().getPath());
		String jarDir = jarFile.getParentFile().getPath();
		return jarDir;
	}
	 
}	
