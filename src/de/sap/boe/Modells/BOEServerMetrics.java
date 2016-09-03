package de.sap.boe.Modells;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;



public class BOEServerMetrics implements Iterable<BOEServerMetric> {
  private static final long      serialVersionUID = 1L;

  private List<BOEServerMetric>             metrics          = new ArrayList<BOEServerMetric>();

  private HashMap<String, BOEServerMetric>  namebuffer       = new HashMap<String, BOEServerMetric>();

  public BOEServerMetrics() {}

  public Iterator<BOEServerMetric> iterator() {
    return this.metrics.iterator();
  }


  public void remove(BOEServerMetrics removedmetrics) {
    for (final BOEServerMetric removedmetric : removedmetrics) {
      remove(removedmetric);
    }
  }


  public void remove(BOEServerMetric removedmetric) {
    final List<Integer> removedPositions = new ArrayList<Integer>();
    int i = 0;
    for (final BOEServerMetric metric : this.metrics) {
      if (metric.equals(removedmetric)) {
        removedPositions.add(i);
      }
      i++;
    }

    for (final Integer removedPosition : removedPositions) {
      this.metrics.remove(removedPosition.intValue());
    }
  }


  public void add(BOEServerMetric newmetric) {
    for (final BOEServerMetric metric : this.metrics) {
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




  public BOEServerMetric get(String name) {
    if (this.namebuffer.containsKey(name)) {
      return this.namebuffer.get(name);
    }

    for (final BOEServerMetric metric : this.metrics) {
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

    for (final BOEServerMetric _metric : this.metrics) {
      if (name.equals(_metric.getName())) {
        this.namebuffer.put(name, _metric);

        return true;
      }
    }

    return false;
  }


  public boolean contains(BOEServerMetric metric) {
    final String name = metric.getName();

    return contains(name);
  }


  public String toString() {
    return this.metrics.toString();
  }
  
  public ArrayList<String> getTableDef(){
	  ArrayList<String> listOfArrays=new ArrayList<String>();
	  listOfArrays.add("tbl_metrics");
	  listOfArrays.add("si_date|TIMESTAMP| |NOT NULL");
	  listOfArrays.add("si_id|integer| |NOT NULL");
	  listOfArrays.add("Server_name|varchar|255|NOT NULL");
	  listOfArrays.add("mname|varchar|255| ");
	  listOfArrays.add("mvalue|varchar|1024| ");
	  listOfArrays.add("si_cluster|varchar|255| " );
	  
	  return listOfArrays;
  }
  
  public String getSQLPK(){
	  return "CREATE UNIQUE INDEX ind_metrics_pk on tbl_metrics(si_date, Server_name, mname, si_cluster)";
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
