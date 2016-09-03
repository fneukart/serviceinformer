package de.sap.boe;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import de.sap.boe.Modells.BOEServers;

//import de.sap.boe.Tools.GUI;
import de.sap.boe.Tools.ServiceInformerActions;
import de.sap.boe.Tools.ServiceInformerServerMonitorActions;

public class ServiceInformer extends Object  {
	  /**
	   * Logger for this class
	   */
	  public static Logger log      = Logger.getLogger(ServiceInformer.class);
	  
	   /**
	    * Define a new boeControl  
	    */
	  public final static ServiceInformer po = new ServiceInformer();
	  
	  /**
	   * global parameters
	   */
	  public BOEServers boeservers = new BOEServers();
	  public static IEnterpriseSession session	  = null;
	  public static Properties properties = new Properties();
		  
	  public static String propertiesPath = "";
	  
	  /**
	   * Calendare for DB-Statements
	   */
	  public static Calendar boCal = new GregorianCalendar();
	  
	
	  //public static GUI gui = new GUI();
	  
	  /**
	    * Initialize a new BOEMonitor  
	    */	
	  private void init (String[] args) {
		  PropertyConfigurator.configure("log4jSI.properties");
		  String propertiesFile = "serviceInformer.properties";
		  String parm = "";
		  String value= "";
		  int pos=0;
		  
		  for (int a=0; a < args.length; a++){
			  pos   = args[a].indexOf("=");
			  parm  = args[a].substring(0, pos);
			  value = args[a].substring(pos+1);
			  properties.setProperty(parm, value);
		  }
		  if(properties.getProperty("Propertiespath")!=null && !"".equals(properties.getProperty("Propertiespath"))){
			  propertiesPath=properties.getProperty("Propertiespath");
		  }
		  else{
			  propertiesPath="./";
		  }
		  
		  if (properties.getProperty("Propertiesfilename")!=null && !"".equals(properties.getProperty("Propertiesfilename"))){
			  propertiesFile=properties.getProperty("Propertiesfilename");
		  }
		  
		  String propertiesFCFile = propertiesPath + propertiesFile;
		    try {
		    	properties.load(new FileInputStream(propertiesFCFile));   	
		    } catch (IOException e)
			{
			}
		    
		    for (int i = 0; i < args.length; i++) {
		    	String sarg = args[i];
		    	log.debug(sarg);
		    	int j = sarg.indexOf("=");
		    	String parmname = sarg.substring(0, j);
		    	String parval = sarg.substring(j+1,sarg.length()); 
		    	properties.setProperty(parmname, parval);
		    }
		    
		    if (properties.getProperty("application.property_print").equalsIgnoreCase("true")){
	    		this.logproperties();
	    	}
	  }	   
	  
	  /**
	   * Main function for running from the command line.<p>
	   *
	   * @param args Command line parameters. If you specify
	   * parameters on the command line that have the same
	   * spelling as the ones specified in the <code>programobject.properties</code>
	   * file, the values from the command line take precedence.
	   *
	   * @throws SDKException
	   */
	  @SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		log.info("=====================================================================");  
		log.debug("Start");
		po.init(args);	    							 		
		
		//ServerManager
		if(Boolean.parseBoolean(properties.getProperty("application.ServerManager"))){
		  po.connectToBOE(0);
			ServiceInformerActions boSA;
			boSA = new ServiceInformerActions(po);
			boSA.changeServerState();
		}
		
	    //MonitorServers
		if(Boolean.parseBoolean(properties.getProperty("application.MonitorServers"))){
		  po.connectToBOE(0);
			ServiceInformerServerMonitorActions boSMA;
			boSMA = new ServiceInformerServerMonitorActions(po);
			boSMA.loopreadStatus();
		}
		try {session.logoff();}
		catch (Exception s){		
		}
		//dblog.destroy();
		
		System.out.println("Fertig!");
		log.debug("=====================================================================");
		log.debug("F E R T I G");
		log.info("=====================================================================");
	  }
	  
	  
	  public static void connectToBOE(int i){
		  try {
			  session.getClusterName();
		  }
		  catch (Exception e){
			  try {				  
				    i++;
					log.debug("Starting Logging on");
					session = CrystalEnterprise.getSessionMgr().logon(properties.getProperty("application.BOE.username"), 
												  properties.getProperty("application.BOE.password"), 
												  properties.getProperty("application.BOE.cms"), 
												  properties.getProperty("application.BOE.authenticationtype"));
					log.debug("loggedin");
					log.info("Logged on to " + session.getClusterName());
				}
				catch (SDKException sdke){
					log.error("!Error while logon!");
					log.error(sdke.getLocalizedMessage());
					log.error(sdke.getMessage());
					if (sdke.getErrorCodeString().equals("FWM 01003")&& i < 5){
						log.warn("trying to reconnect.");
						po.sleep(Integer.parseInt(ServiceInformer.properties.getProperty("application.timeout")));
						connectToBOE(i);
					}
					if (sdke.getErrorCodeString().equals("FWM 01003")&& i >= 5){
						log.fatal("CMS not reachable! Stop trying to connect!");
					}
				}
		  }
	  }
	  
	  
	   /**
	    * Printout properties only if loglevel is debug  
	    */		  
	  @SuppressWarnings("unchecked")
	private void logproperties (){
//		  log.info("-------------------------------------------------------");
//		  log.info("printout properties");
//		  log.info("-------------------------------------------------------");
//		  for (Enumeration e = properties.propertyNames();  e.hasMoreElements(); ){
//			  String proname = e.nextElement().toString();
//			  log.info(proname + ": " + properties.getProperty(proname));			  
//		  }
		  log.info("-------------------------------------------------------");
	  }
	  
	  
	 public void sleep(long isleep){
		 try {
			  Thread.sleep(isleep);
		 
		  } catch (InterruptedException ie){
			  //th Vm does not want us to sleep any more
		  }
	 }
}
