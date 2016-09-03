package de.sap.boe.Modells;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;



public class ServiceInformerProperties implements Iterable<ServiceInformerProperty> {
  private static final long      serialVersionUID = 1L;

  private List<ServiceInformerProperty>             metrics          = new ArrayList<ServiceInformerProperty>();

  private HashMap<String, ServiceInformerProperty>  namebuffer       = new HashMap<String, ServiceInformerProperty>();

  public ServiceInformerProperties() {}

  public Iterator<ServiceInformerProperty> iterator() {
    return this.metrics.iterator();
  }


  public void remove(ServiceInformerProperties removedmetrics) {
    for (final ServiceInformerProperty removedmetric : removedmetrics) {
      remove(removedmetric);
    }
  }


  public void remove(ServiceInformerProperty removedmetric) {
    final List<Integer> removedPositions = new ArrayList<Integer>();
    int i = 0;
    for (final ServiceInformerProperty metric : this.metrics) {
      if (metric.equals(removedmetric)) {
        removedPositions.add(i);
      }
      i++;
    }

    for (final Integer removedPosition : removedPositions) {
      this.metrics.remove(removedPosition.intValue());
    }
  }


  public void add(ServiceInformerProperty newmetric) {
    for (final ServiceInformerProperty metric : this.metrics) {
      if (metric.equals(newmetric)) {
 
        if (metric.getValue() == null) {
            final Object nValue = newmetric.getValue();
            if (nValue != null) {
            	metric.setValue(nValue);
            }
        }
        
        if (metric.getObjecttype() == null) {
            final String nObjecttype = newmetric.getObjecttype();
            if (nObjecttype != null) {
            	metric.setObjecttype(nObjecttype);
            }
        }
        return;
      }
    }


    if (newmetric.getName() != null) {
      this.namebuffer.put(newmetric.getName(), newmetric);
    }

    this.metrics.add(newmetric);
  }




  public ServiceInformerProperty get(String name) {
    if (this.namebuffer.containsKey(name)) {
      return this.namebuffer.get(name);
    }

    for (final ServiceInformerProperty metric : this.metrics) {
      if (name.equals(metric.getName())) {
        this.namebuffer.put(name, metric);

        return metric;
      }
    }

    return null;
  }



  public boolean contains(String name) {
    if (name == null) {
      return false;
    }

    if (this.namebuffer.containsKey(name)) {
      return true;
    }

    for (final ServiceInformerProperty _metric : this.metrics) {
      if (name.equals(_metric.getName())) {
        this.namebuffer.put(name, _metric);

        return true;
      }
    }

    return false;
  }


  public boolean contains(ServiceInformerProperty metric) {
    final String name = metric.getName();

    return contains(name);
  }


  public String toString() {
    return this.metrics.toString();
  }
  
  public ArrayList<String> getTableDef(){
	  ArrayList<String> listOfArrays=new ArrayList<String>();
	  listOfArrays.add("tbl_Sproperties");
	  listOfArrays.add("si_date|TIMESTAMP| |not null");
	  listOfArrays.add("si_id|integer| |not null");
	  listOfArrays.add("Server_name|varchar|255|not null");
	  listOfArrays.add("mname|varchar|255| ");
	  listOfArrays.add("mvalue|varchar|1024| ");
	  listOfArrays.add("objecttype|varchar|255| ");
	  listOfArrays.add("si_cluster|varchar|255| " );
	  
	  return listOfArrays;
  }
  
  public String getSQLPK(){
	  return "CREATE UNIQUE INDEX ind_Sproperties_pk on tbl_Sproperties(si_date, Server_name, mname, si_cluster)";
  }
  
  public ArrayList<String> getColumns(){
	  ArrayList<String> list=new ArrayList<String>();
	  ArrayList<String> tableDef = getTableDef();
	  for (int i = 1; i<tableDef.size() ; i++ ){
		  StringTokenizer st = new StringTokenizer(tableDef.get(i),"|");
		  list.add(st.nextToken());
	  }
	  return list;
  }
  
  public String getTablename(){
	  return getTableDef().get(0);
  }
  
  public ArrayList<String> getColumnDefs(){
	  ArrayList<String> list=new ArrayList<String>();
	  ArrayList<String> tableDef = getTableDef();
	  for (int i = 1; i<tableDef.size() ; i++ ){
		  StringTokenizer st = new StringTokenizer(tableDef.get(i),"|");
		  st.nextElement();
		  st.nextElement();
		  list.add(st.nextToken());
	  }
	  return list;
  }
  
  public ArrayList<String> getColumnTypes(){
	  ArrayList<String> list=new ArrayList<String>();
	  ArrayList<String> tableDef = getTableDef();
	  for (int i = 1; i<tableDef.size() ; i++ ){
		  StringTokenizer st = new StringTokenizer(tableDef.get(i),"|");
		  st.nextElement();
		  list.add(st.nextToken());
	  }
	  return list;
  }   
  
}
