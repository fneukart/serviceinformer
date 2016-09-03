package de.sap.boe.Tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.businessobjects.sdk.plugin.desktop.common.IConfigProperties;
import com.businessobjects.sdk.plugin.desktop.common.IConfigProperty;

import com.businessobjects.sdk.plugin.desktop.common.IConfiguredService;
import com.businessobjects.sdk.plugin.desktop.common.IMetric;
import com.businessobjects.sdk.plugin.desktop.common.IMetrics;
import com.businessobjects.sdk.plugin.desktop.metricdescriptions.IMLDescriptions;
import com.businessobjects.sdk.plugin.desktop.metricdescriptions.IMetricDescriptions;
import com.businessobjects.sdk.plugin.desktop.metricdescriptions.IPropertyRenderTemplate;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;
import com.crystaldecisions.sdk.plugin.desktop.server.IServerMetrics;


import de.sap.boe.ServiceInformer;
import de.sap.boe.Modells.*;

public class ServiceInformerServerMonitorActions {
	private Logger log;
	private ServiceInformer po;
	String clustername ;

	private HashMap<String, String> servers2Log = new HashMap<String, String>();
	boolean metricFilter=false;

	public ServiceInformerServerMonitorActions (ServiceInformer gpo) {
		this.po = gpo;
		this.log= ServiceInformer.log;
		getServerMetricFilterfromFile();
		clustername = ServiceInformer.session.getClusterName();
	}	


	@SuppressWarnings("static-access")
	public void readStatus(){
		this.po.boCal.setTime(new Date());

		BOEServers boeServers  = new BOEServers();	  

		String datum = this.po.boCal.get(Calendar.DATE) + "." +
		(this.po.boCal.get(Calendar.MONTH)+1) + "." +
		this.po.boCal.get(Calendar.YEAR);
		String zeit = datum + " " + this.po.boCal.get(Calendar.HOUR_OF_DAY) + ":" +
		this.po.boCal.get(Calendar.MINUTE) + ":" +
		this.po.boCal.get(Calendar.SECOND);

		ServiceInformer.connectToBOE(0);

		readStatusdetail(zeit, boeServers);

		//application.MonitorServers.Log2DB
		//		  if(Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.Log2DB"))){
		//			  write2DB(boeServers);			  
		//		  }

		writestatus(boeServers);

		//if(Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.Display"))){
		write2Display(boeServers, zeit);
		//}
	}


	public void loopreadStatus(){

		long refreshintervall  = Long.parseLong(ServiceInformer.properties.getProperty("application.MonitorServers.refresh_intervall"));
		boolean looprefresh 	 = true;

		while (looprefresh){
			readStatus();

			looprefresh = Boolean.parseBoolean(ServiceInformer.properties.getProperty("application.MonitorServers.looprefresh"));

			if (looprefresh){
				po.sleep(refreshintervall);
			}				  
		}
	}


	/**
	 * Reads and logs the status of the services  
	 */		  
	@SuppressWarnings({ "static-access", "unchecked" })
	private void readStatusdetail (String izeit, BOEServers boeservers){
		BOEServer boeServer = null;

		String metricDescriptionsQuery;
		IMetricDescriptions metricDescriptions;
		//IServerMetrics serverMetrics;	
		Set serviceNames;
		Iterator serviceNamesIter;

		try{
			log.debug("EnterpriseVersion: " + ServiceInformer.session.getEnterpriseVersion());

			IInfoStore iSStore = (IInfoStore) ServiceInformer.session.getService("InfoStore");
			IInfoStore iMStore = (IInfoStore) ServiceInformer.session.getService("InfoStore");
			if (ServiceInformer.session!= null)
			{ 
				String strQuery = "Select * from CI_SYSTEMOBJECTS where SI_kind ='Server'";
				IInfoObjects iServers = (IInfoObjects) iSStore.query(strQuery);
				//Abfrage der Servercategories
				//Select si_member_services, si_name from ci_systemobjects where SI_parentid=61 

				metricDescriptionsQuery = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND='MetricDescriptions'";
				metricDescriptions = (IMetricDescriptions) iMStore.query(metricDescriptionsQuery).get(0);

				for(int i = 0 ; i < iServers.size(); i++){
					IServer iServer = (IServer) iServers.get(i);
					boeServer = new BOEServer(iServer.getID(), 
							iServer.getName(), 
							iServer.getPID(),
							iServer.getSIAHostname(),
							iServer.getFriendlyName(),
							iServer.getState().toString() ,
							iServer.getCurrentDisabledState(),
							iServer.getAutoBoot(), 
							iServer.getServerKind(),
							this.po.boCal.getTime() );
					boeservers.add(boeServer);

					//configuration
					//if(Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.Configuration"))){
					boeServer.getServiceInformerProperties().add(new ServiceInformerProperty("CurrentCommandLine",
							iServer.getCurrentCommandLine(),
							iServer.getCurrentCommandLine().getClass().toString()));
					boeServer.getServiceInformerProperties().add(new ServiceInformerProperty("AutoBoot",
							Boolean.valueOf(iServer.getAutoBoot()),
							Boolean.valueOf(iServer.getAutoBoot()).getClass().toString()));
					Iterator iHSIter = iServer.getHostedServices().iterator();

					while(iHSIter.hasNext()){
						IConfiguredService service= (IConfiguredService) iHSIter.next();		    				    			    			
						IConfigProperties iConfProp = service.getConfigProps();
						Iterator iConfPropIter = iConfProp.iterator();
						while(iConfPropIter.hasNext()){	    				
							IConfigProperty iConfPropElem = (IConfigProperty) iConfPropIter.next();
							if (iConfPropElem.getValue().getClass().toString().equals("class com.businessobjects.sdk.plugin.desktop.common.internal.ConfigProperties")){
								readProperties((IConfigProperties)iConfPropElem.getValue(), boeServer, iConfPropElem.getName());
							}
							else {
								ServiceInformerProperty boeControlProperty = new ServiceInformerProperty(iConfPropElem.getName(),
										iConfPropElem.getValue(),
										iConfPropElem.getValue().getClass().toString());
								boeServer.getServiceInformerProperties().add(boeControlProperty);
							}

						}
					}
					//}


					//--Metrics
					//if(Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.Metrics"))){
					IServerMetrics imetrics = (IServerMetrics) iServer.getMetrics();
					serviceNames = imetrics.getServiceInterfaceNames();
					serviceNamesIter = serviceNames.iterator();


					// Display the metrics for each service.
					while(serviceNamesIter.hasNext()) {
						String serviceName = (String) serviceNamesIter.next();
						IMetrics serviceMetrics = imetrics.getMetrics(serviceName);

						Iterator serviceMetricsIter = serviceMetrics.iterator();
						while(serviceMetricsIter.hasNext()) {
							boolean readSM = true;
							IMetric metric = (IMetric) serviceMetricsIter.next();
							String metricName = metric.getName();

							// Get the localized metric name.
							IMLDescriptions descriptions = metricDescriptions.getMetricDescriptions(serviceName);
							IPropertyRenderTemplate propertyRenderTemplate = descriptions.getPropertyRenderTemplate(metricName);
							String localizedMetricName = propertyRenderTemplate.getLabel(Locale.GERMAN);
							if(metricFilter) {
								readSM="true".equals(servers2Log.get(iServer.getServerKind() + "." + localizedMetricName));
							}


							if (readSM){
								BOEServerMetric boesm = new BOEServerMetric(localizedMetricName, 
										metric.getValue(), 
										metric.getValue().getClass().toString());

								boeServer.getBOEServerMetrics().add(boesm);
								//log.debug("|--> Server:" + boeServer.getName() + " | Metric: " + localizedMetricName + " = " + metric.getValue() );
							}

						}
					}
					// End Metrics
					//}

				}
			}
		}
		catch (Exception se){
			log.error(se.getLocalizedMessage());
			log.error(se.getMessage() );
		}

	}

	private void writestatus(BOEServers boeservers){
		BOEServerMetrics boeservermetrics = new BOEServerMetrics();
		// print_all
		//if (Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.print_all"))){
		for (final BOEServer boeserver : boeservers){
			log.warn(boeserver.getStatus() + " " 
					+ boeserver.getName() + " on " 
					+ boeserver.getSIA() + " ( PID=" 
					+ boeserver.getPID() + " ) | server is enabled = " 
					+ !boeserver.getDisabled() );	
			//if (Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.print_metric"))){
			boeservermetrics = boeserver.getBOEServerMetrics();
			for (final BOEServerMetric boeservermetric : boeservermetrics){
				log.info("|--> " + boeservermetric.getName() + " = " + boeservermetric.getValue() );	
			}  
			//}
		}			  
		//}

		//print_stopped
		//if (Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.print_stopped"))){
		for (final BOEServer boeserver : boeservers){
			if (!boeserver.getStatus().equalsIgnoreCase("running")){	
				log.warn(boeserver.getStatus() + " " 
						+ boeserver.getName() + " on " 
						+ boeserver.getSIA() + " ( PID=" 
						+ boeserver.getPID() + " ) | server is enabled = " 
						+ !boeserver.getDisabled() );		
				//if (Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.print_metric"))){
				boeservermetrics = boeserver.getBOEServerMetrics();
				for (final BOEServerMetric boeservermetric : boeservermetrics){
					log.info("|--> " + boeservermetric.getName() + " = " + boeservermetric.getValue() );	
				}  
				// }
			}
		}			  
		//}

		//print_started
		//if (Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.print_started"))){
		for (final BOEServer boeserver : boeservers){
			if (boeserver.getStatus().equalsIgnoreCase("running")){	
				log.warn(boeserver.getStatus() + " " 
						+ boeserver.getName() + " on " 
						+ boeserver.getSIA() + " ( PID=" 
						+ boeserver.getPID() + " ) | server is enabled = " 
						+ !boeserver.getDisabled() );	
				//if (Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.print_metric"))){
				boeservermetrics = boeserver.getBOEServerMetrics();
				for (final BOEServerMetric boeservermetric : boeservermetrics){
					log.info("|--> " + boeservermetric.getName() + " = " + boeservermetric.getValue() );	
				}  
				// }
			}
		}			  
		//}

		//print_enabled_stopped
		//if (Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.print_enabled_stopped"))){
		for (final BOEServer boeserver : boeservers){
			if (!boeserver.getStatus().equalsIgnoreCase("running") && !boeserver.getDisabled()){	
				log.warn(boeserver.getStatus() + " " 
						+ boeserver.getName() + " on " 
						+ boeserver.getSIA() + " ( PID=" 
						+ boeserver.getPID() + " ) | server is enabled = " 
						+ !boeserver.getDisabled() );
				//if (Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.print_metric"))){
				boeservermetrics = boeserver.getBOEServerMetrics();
				for (final BOEServerMetric boeservermetric : boeservermetrics){
					log.info("|--> " + boeservermetric.getName() + " = " + boeservermetric.getValue() );	
				}  
				// }
			}
		}			  
		//}

		//print_disabled
		//if (Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.print_disabled"))){
		for (final BOEServer boeserver : boeservers){
			if (boeserver.getDisabled()){	
				log.warn(boeserver.getStatus() + " " 
						+ boeserver.getName() + " on " 
						+ boeserver.getSIA() + " ( PID=" 
						+ boeserver.getPID() + " ) | server is enabled = " 
						+ !boeserver.getDisabled() );	
				// if (Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.print_metric"))){
				boeservermetrics = boeserver.getBOEServerMetrics();
				for (final BOEServerMetric boeservermetric : boeservermetrics){
					log.info("|--> " + boeservermetric.getName() + " = " + boeservermetric.getValue() );	
				}  
				//}
			}
		}			  
		//}

		//print_enabled
		//if (Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.print_disabled"))){
		for (final BOEServer boeserver : boeservers){
			if (!boeserver.getDisabled()){	
				log.warn(boeserver.getStatus() + " " 
						+ boeserver.getName() + " on " 
						+ boeserver.getSIA() + " ( PID=" 
						+ boeserver.getPID() + " ) | server is enabled = " 
						+ !boeserver.getDisabled() );	
				//if (Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.print_metric"))){
				boeservermetrics = boeserver.getBOEServerMetrics();
				for (final BOEServerMetric boeservermetric : boeservermetrics){
					log.info("|--> " + boeservermetric.getName() + " = " + boeservermetric.getValue() );	
				}  
				// }
			}
		}			  
		//}

	}



	private void getServerMetricFilterfromFile() {
		String propertiesFile;
		String propertiesFCFile;
		propertiesFile = "metric_selection.csv";
		propertiesFCFile = ServiceInformer.propertiesPath + propertiesFile;

		File file = new File(propertiesFCFile);

		try {
			BufferedReader bufRdr = new BufferedReader(new FileReader(file));

			//read each line of text file but skip first one
			int i = 0;
			int j = 0;
			String line = null;

			while((line = bufRdr.readLine()) != null)
			{
				log.debug(i+": " +line);
				if (i>0){
					String kind;
					String metric;
					boolean tolog;

					StringTokenizer st = new StringTokenizer(line,",");
					kind=st.nextToken();
					metric=st.nextToken();
					tolog="true".equals(st.nextToken());
					if(tolog) {
						servers2Log.put(kind + "." + metric, ""+tolog);
						j++;
						metricFilter=true;
					}						
				}
				i++;
			}
			log.info("Es wurden " + (j) + " server metric filter eingelesen.");
			bufRdr.close();
		}
		catch(Exception e){
			log.error("!Error while read server metric filter !");
			log.error(e.getLocalizedMessage());
			log.error(e.getMessage());
		}

	}

	private void readProperties (IConfigProperties iConfProp, BOEServer boeServer, String prefix){
		Iterator iConfPropIter = iConfProp.iterator();
		while(iConfPropIter.hasNext()){	    				
			IConfigProperty iConfPropElem = (IConfigProperty) iConfPropIter.next();
			ServiceInformerProperty boeControlProperty = new ServiceInformerProperty(prefix +": " + iConfPropElem.getName(),
					iConfPropElem.getValue(),
					iConfPropElem.getValue().getClass().toString());
			boeServer.getServiceInformerProperties().add(boeControlProperty);
		}
	}

	private void write2Display(BOEServers boeservers, String zeit){
		String EnterpriseVersion="";
		String Clustername="";

		//sortieren
		ArrayList<BOEServer> ArrayRunningEnabled=new ArrayList<BOEServer>();
		ArrayList<BOEServer> ArrayRunningDisabled=new ArrayList<BOEServer>();
		ArrayList<BOEServer> ArrayStoppedEnabled=new ArrayList<BOEServer>();
		ArrayList<BOEServer> ArrayStoppedDisabled=new ArrayList<BOEServer>();
		for (final BOEServer boeserver : boeservers){
			if (boeserver.getStatus().equalsIgnoreCase("running")){
				if (boeserver.getDisabled()){
					ArrayRunningDisabled.add( boeserver);
				}
				else{
					ArrayRunningEnabled.add( boeserver);
				}
			}
			else{
				if (boeserver.getDisabled()){
					ArrayStoppedDisabled.add( boeserver);
				}
				else{
					ArrayStoppedEnabled.add( boeserver);
				}
			}
		}

		try{
			EnterpriseVersion = ""+ServiceInformer.session.getEnterpriseVersion();
			Clustername = ServiceInformer.session.getClusterName();
		}
		catch(Exception e){
			log.error(e.getLocalizedMessage());
			log.error(e.getMessage());
		}

		//application.MonitorServers.Display.Console=true
		//if (Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.Display.Console"))){
		System.out.println("");
		System.out.println("Lauf: " + zeit);
		System.out.println("Cluster: " + Clustername);
		System.out.println("EnterpriseVersion: " + EnterpriseVersion);
		System.out.println("");
		for (final BOEServer boeserver : boeservers){
			System.out.println(boeserver.getStatus() + ", " + !boeserver.getDisabled() + ", " + boeserver.getName() + ", " + boeserver.getSIA() + ", PID:" + boeserver.getPID());
		}

		//}
		// application.MonitorServers.Display.GUI=true
		//if (Boolean.parseBoolean(boeControl.properties.getProperty("application.MonitorServers.Display.GUI"))){
		//this.po.gui.setVisible(true);

		String output = new String();
		String col = new String();
		output = "<html>";
		output = output + "Lauf:              " + zeit+ "<br>";
		output = output + "Cluster:           " + Clustername+ "<br>";
		output = output + "EnterpriseVersion: " + EnterpriseVersion+ "<br>";
		output = output + "-----------------------------------------------------<br>";
		output = output+"</html>";
		//this.po.gui.setHeading(output);

		output = "<html>";
		//StoppedEnabled = rot
		col="</b><font color=red><b>";
		for (int i=0;  i < ArrayStoppedEnabled.size() ; i++){
			BOEServer boeserver = ArrayStoppedEnabled.get(i);
			output = output + col + boeserver.getStatus() + ", " + !boeserver.getDisabled() + ", " + boeserver.getName() + ", " + boeserver.getSIA() + ", PID:" + boeserver.getPID() +"</b></i></font><br>";
		}
		//RunningDisabled = orange
		col="</b><font color=#FF8040><i>";
		for (int i=0;  i < ArrayRunningDisabled.size() ; i++){
			BOEServer boeserver = ArrayRunningDisabled.get(i);
			output = output + col + boeserver.getStatus() + ", " + !boeserver.getDisabled() + ", " + boeserver.getName() + ", " + boeserver.getSIA() + ", PID:" + boeserver.getPID() +"</b></i></font><br>";
		}
		//RunningEnabled = orange
		col="</b><font color=green>";
		for (int i=0;  i < ArrayRunningEnabled.size() ; i++){
			BOEServer boeserver = ArrayRunningEnabled.get(i);
			output = output + col + boeserver.getStatus() + ", " + !boeserver.getDisabled() + ", " + boeserver.getName() + ", " + boeserver.getSIA() + ", PID:" + boeserver.getPID() +"</b></i></font><br>";
		}
		//StoppedDisabled = blue
		col="</b><font color='blue' size='-2'><i>";
		for (int i=0;  i < ArrayStoppedDisabled.size() ; i++){
			BOEServer boeserver = ArrayStoppedDisabled.get(i);
			output = output + col + boeserver.getStatus() + ", " + !boeserver.getDisabled() + ", " + boeserver.getName() + ", " + boeserver.getSIA() + ", PID:" + boeserver.getPID() +"</b></i></font><br>";
		}
		output = output+"</html>";

		//this.po.gui.setText(output);
		//}

	}


}	  