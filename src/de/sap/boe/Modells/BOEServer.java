package de.sap.boe.Modells;

import java.util.Date;


@SuppressWarnings("unchecked")
public class BOEServer implements Comparable {

	private Integer  id                    = -1;
	private String   name;
	private String 	 Type;
	private Integer  pid = 0;
	private String   SIAHostname;
	private String   FriendlyName;
	private String   Serverkind;
	private String   Status;
	private Date     date;
	private boolean  autoboot = false;
	private boolean  disabled = false;
	private boolean  start_needed=false;
	private boolean  enable_needed=false;
	private String   Servernamefromfile;
	private BOEServerMetrics boeServerMetrics          = new BOEServerMetrics();
	private ServiceInformerProperties serviceInformerProperties  = new ServiceInformerProperties();

	public BOEServer( String name) {
		this.name = name;
	}

	public BOEServer(Integer id, String name) {
		this.id   = id;
		this.name = name;
	}

	public BOEServer(Integer id, String Type, String name) {
		this.id   = id;
		this.Type = Type;
		this.name = name;
		this.Servernamefromfile=name;
	}

	public BOEServer(Integer id, String  name, Integer pid, 
			String  SIAHostname, String FriendlyName, 
			String  Status, boolean disabled) {
		this.id = id;
		this.name = name;
		this.pid = pid;
		this.SIAHostname= SIAHostname;
		this.FriendlyName = FriendlyName;
		this.Status = Status ;
		this.disabled = disabled;
		this.date = new Date();
	}

	public BOEServer(Integer id, String  name, Integer pid, 
			String  SIAHostname, String FriendlyName, 
			String  Status, boolean disabled,
			Date idate, BOEServerMetrics boeServerMetrics) {
		this.id = id;
		this.name = name;
		this.pid = pid;
		this.SIAHostname= SIAHostname;
		this.FriendlyName = FriendlyName;
		this.Status = Status ;
		this.disabled = disabled;
		this.date = idate;
		this.boeServerMetrics = boeServerMetrics;
	}

	public BOEServer(Integer id, String  name, Integer pid, 
			String  SIAHostname, String FriendlyName, 
			String  Status, boolean disabled,
			Date idate) {
		this.id = id;
		this.name = name;
		this.pid = pid;
		this.SIAHostname= SIAHostname;
		this.FriendlyName = FriendlyName;
		this.Status = Status ;
		this.disabled = disabled;
		this.date = idate;
	}

	public BOEServer(Integer id, String  name, Integer pid, 
			String  SIAHostname, String FriendlyName, 
			String  Status, boolean disabled, Boolean autoboot, String kind, 
			Date idate) {
		this.id = id;
		this.name = name;
		this.pid = pid;
		this.SIAHostname= SIAHostname;
		this.FriendlyName = FriendlyName;
		this.Status = Status ;
		this.disabled = disabled;
		this.Serverkind=kind;
		this.autoboot=autoboot;
		this.date = idate;
	}


	public int compareTo(Object other) throws ClassCastException {
		if (!(other instanceof BOEServer)) {
			throw new ClassCastException("Cannot compare apples with oranges.");
		}
		return this.name.compareTo(((BOEServer) other).getName());
	}


	// ID	  
	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}


	// Name
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	// Type
	public void setType(String Type) {
		this.Type = Type;
	}


	public String getType() {
		return this.Type;
	}



	// PID
	public void setPID(int id) {
		this.pid = id;
	}

	public int getPID() {
		return this.pid;
	}


	// SIAHost
	public void setSIA(String sia) {
		this.SIAHostname = sia;
	}

	public String getSIA() {
		return this.SIAHostname;
	}


	//	FriendlyName
	public void setFriendlyName(String FriendlyName) {
		this.FriendlyName = FriendlyName;
	}

	public String getFriendlyName() {
		return this.FriendlyName;
	}

	//	FriendlyName
	public void setServernamefromfile(String Servernamefromfile) {
		this.Servernamefromfile = Servernamefromfile;
	}

	public String getServernamefromfile() {
		return this.Servernamefromfile;
	}

	//	Serverkind
	public void setServerkind(String Serverkind) {
		this.Serverkind = Serverkind;
	}

	public String getServerkind() {
		return this.Serverkind;
	}	


	// Status
	public void setStatus(String status) {
		this.Status = status;
	}


	public String getStatus() {
		return this.Status;
	}


	// disabled
	public void setAutoboot(boolean autoboot) {
		this.autoboot = autoboot;
	}

	public boolean getAutoboot() {
		return this.autoboot;
	}


	// disabled
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean getDisabled() {
		return this.disabled;
	}


	// start_needed;
	public void setStartneeded(boolean start){
		this.start_needed=start;
	}

	public boolean getStartneeded(){
		return this.start_needed;
	}

	//	 enable_needed;
	public void setenableneeded(boolean start){
		this.enable_needed=start;
	}

	public boolean getenableneeded(){
		return this.enable_needed;
	}	


	// date
	public void setDate(Date iDate){
		this.date = iDate;
	}

	public Date getDate(){
		return this.date;
	}

	//boeServers
	public void setBOEServerMetrics(BOEServerMetrics boeServerMetrics){
		this.boeServerMetrics = boeServerMetrics;
	}

	public BOEServerMetrics getBOEServerMetrics(){
		return this.boeServerMetrics;
	} 	  

	//serviceInformerProperties	  
	public void setServiceInformerProperties(ServiceInformerProperties serviceInformerProperties){
		this.serviceInformerProperties = serviceInformerProperties;
	}

	public ServiceInformerProperties getServiceInformerProperties(){
		return this.serviceInformerProperties;
	} 	  



	public String toString() {
		return "[" + this.id + "=" + this.name + "]";
	}

}

