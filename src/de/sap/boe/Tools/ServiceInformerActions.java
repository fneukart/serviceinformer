package de.sap.boe.Tools;

import java.io.*;
import java.net.InetAddress;
import java.util.*;

import org.apache.log4j.Logger;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.server.ExpectedRunState;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;

import de.sap.boe.ServiceInformer;
import de.sap.boe.Modells.*;

public class ServiceInformerActions {
	  private Logger log;
	  private ServiceInformer po;
	  

	 public ServiceInformerActions (ServiceInformer gpo) {
		  this.po = gpo;
		  this.log= ServiceInformer.log;
	  }	  
	  
	  public void changeServerState() throws Exception {
		log.info("=====================================================================");  
		log.debug("Start ");

		getServerListfromFile();

		manage_services();

		//printServerartions();	
		ServiceInformerServerMonitorActions boSMA;
		boSMA = new ServiceInformerServerMonitorActions(po);
		boSMA.readStatus();

		System.out.println("Fertig!");
		log.debug("=====================================================================");
		log.debug("F E R T I G");
		log.debug("=====================================================================");
	  }
	  
	  
	  
	  private void getServerListfromFile() throws Exception{
		   String propertiesFile;
		   String propertiesFCFile;
		   propertiesFile = ServiceInformer.properties.getProperty("application.ServerManager.filelistofservers");
		   propertiesFCFile = ServiceInformer.propertiesPath + propertiesFile;

			File file = new File(propertiesFCFile);
			
			try {
				BufferedReader bufRdr = new BufferedReader(new FileReader(file));
			
				//read each line of text file but skip first one
				int i = 0;
				String line = null;
			
				String hostname= InetAddress.getLocalHost().getHostName();
				String sia=ServiceInformer.properties.getProperty("application.ServerManager.sia");
				if (sia=="" || sia.equals(null)){
					sia="sia" + hostname;
				}
				else{
					int j = sia.indexOf("<");
					if (j>=0){
						sia = sia.substring(j+1,sia.length()-1);
						sia = sia + hostname;
					}
				}
							
				log.info("SIA: "+ sia);
				// parse the read lines to BOEServer-Hashmap
				while((line = bufRdr.readLine()) != null)
				{
					log.debug(line);
					if (i>0){
						ArrayList<String> myArr = new ArrayList<String>();
						StringTokenizer st = new StringTokenizer(line,",");
						while (st.hasMoreTokens()){
							myArr.add(st.nextToken());
						}
						BOEServer server=new BOEServer(Integer.parseInt(myArr.get(0)), myArr.get(1), myArr.get(2));
						server.setSIA(sia);
						server.setenableneeded(Boolean.parseBoolean(myArr.get(3)));
						server.setServernamefromfile( myArr.get(2));
						//what is the desired status for the server
						mark_server(server, ServiceInformer.properties.getProperty("application.ServerManager.currentscenario"));
						
						po.boeservers.add(server);
					}
					i++;
				}
				log.info("Es wurden " + (i-1) + " Server eingelesen.");
				bufRdr.close();
			}
			catch(Exception e){
				log.error("!Error while read serverlist!");
				log.error(e.getLocalizedMessage());
				log.error(e.getMessage());
				throw e;
			}
			  
	  }

	  /*
	   * Reads the servers from the CMS and calls serveraction
	   */
	  private void manage_services(){
		  
		try{
			IInfoStore iSStore = (IInfoStore) ServiceInformer.session.getService("InfoStore");
			
			if (ServiceInformer.session!= null)
	   		{ 
	   			String strQuery = "Select * from CI_SYSTEMOBJECTS where SI_kind ='Server'";
	   			IInfoObjects iServers = (IInfoObjects) iSStore.query(strQuery);
	   			for(int i = 0 ; i < iServers.size(); i++){
	    			IServer iServer = (IServer) iServers.get(i);
	    			log.debug(iServer.getID() + " - " + 
	    					 iServer.getName() + "|"+
	    					 iServer.getFriendlyName());
	    			serveraction(iServer);
	    			
	   			}
	   		}
		
		}
		catch (Exception se){
	   		log.error(se.getLocalizedMessage());
	   		log.error(se.getMessage() );			
		}
	  }
	  /*
	   * marks the service to be started or stopped
	   */
	  private void mark_server(BOEServer server, String scenario){
		 String cathegory = ServiceInformer.properties.getProperty(scenario);
		 // Checks is frs servers should be started or not
		 String startfrs = ServiceInformer.properties.getProperty("application.ServerManager.startfrs");
		 boolean bstartfrs = false;
		 if (startfrs=="true" || startfrs.equalsIgnoreCase("true")){
			 bstartfrs=true;
		 }
		 
		 server.setStartneeded(false);
		 //goes through the list of server categories and sets the start-flag
		 StringTokenizer st = new StringTokenizer(cathegory,",");
			while (st.hasMoreTokens()){
				if (server.getType().equals(st.nextToken())){ 
					if (server.getType().equals("frs")){
						server.setStartneeded(bstartfrs);
					}
					else {
						server.setStartneeded(true);
					}
				}
			}
	  }
	  
	  /*
	   * Prints info Server: Type, Startneeded, Enabled, Name, SIA, PID to the log
	   */
	 /* private void printServerartions(){
		  log.info("Server: Type, Startneeded, Enabled, Name, SIA, PID");
		  for (final BOEServer boeserver : po.boeservers){
			  log.info("Server: " + 
					    boeserver.getType()+ ", " + 
					    boeserver.getStartneeded() + ", " +
					    boeserver.getenableneeded() + ", " +
					    boeserver.getName() + ", " +
					    boeserver.getSIA() + ", " +
					    boeserver.getPID());
		  }
	  }*/
	  
	  /*
	   * Starts and stops servers
	   */
	  private void serveraction(IServer iServer){
		  for (final BOEServer boeserver : po.boeservers){
			  String servicename = boeserver.getSIA()+ "." + 
			  					   boeserver.getServernamefromfile();
			  try{				  
				  if(iServer.getName().equalsIgnoreCase(servicename)){
					  if (boeserver.getStartneeded()){
						  IInfoStore iSStore = (IInfoStore) ServiceInformer.session.getService("InfoStore");
						  String strQuery = "Select * From CI_SYSTEMOBJECTS Where SI_NAME ='"+iServer.getName()+"'";;
				   		  IInfoObjects rServer = (IInfoObjects) iSStore.query(strQuery);
				   		  IServer server = (IServer) rServer.get(0);
				   		  boeserver.setId(server.getID());
				   		  boeserver.setName(server.getName());
				   		  boeserver.setFriendlyName(server.getFriendlyName());
				   		  boeserver.setDate(ServiceInformer.boCal.getTime());
						  server.setExpectedRunState(ExpectedRunState.RUNNING);
						  if(Boolean.parseBoolean(ServiceInformer.properties.getProperty("application.ServerManager.setenable"))){
							  server.setDisabled(!boeserver.getenableneeded());  
						  }
						  iSStore.commit(rServer);
						  log.info("Starte Server: "+iServer.getName());
					  }
					  else {
						  IInfoStore iSStore = (IInfoStore) ServiceInformer.session.getService("InfoStore");
						  String strQuery = "Select * From CI_SYSTEMOBJECTS Where SI_NAME ='"+iServer.getName()+"'";;
				   		  IInfoObjects rServer = (IInfoObjects) iSStore.query(strQuery);
				   		  IServer server = (IServer) rServer.get(0);
				   		  boeserver.setId(server.getID());
				   		  boeserver.setName(server.getName());
				   		  boeserver.setFriendlyName(server.getFriendlyName());
				   		  boeserver.setDate(ServiceInformer.boCal.getTime());
				   		  if (server.getServerKind().equals("CMS") || server.getServerKind().equals("aps")){
				   			log.fatal("A CMS CAN NOT BE STOPPED WITH THIS APPLICATION!!!");
				   		  }
				   		  else {
				   			  server.setExpectedRunState(ExpectedRunState.STOPPED);
				   			  if(Boolean.parseBoolean(ServiceInformer.properties.getProperty("application.ServerManager.setenable"))){
								  server.setDisabled(!boeserver.getenableneeded());  
							  }
				   			  iSStore.commit(rServer);
				   			  log.info("Stope Server: "+iServer.getName());
				   		  }
				   		  po.sleep(Integer.parseInt(ServiceInformer.properties.getProperty("application.timeout")));
					  }
				  }
			  }
			  catch (Exception se){
				  log.error(se.getLocalizedMessage());
				  log.error(se.getMessage() );			
			  }
		  }
	  }
	  

}
