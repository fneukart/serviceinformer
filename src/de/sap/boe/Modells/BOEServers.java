package de.sap.boe.Modells;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;



public class BOEServers implements Iterable<BOEServer> {
  private static final long      serialVersionUID = 1L;

  private List<BOEServer>             servers            = new ArrayList<BOEServer>();

  private HashMap<Integer, BOEServer> idbuffer         = new HashMap<Integer, BOEServer>();

  private HashMap<String, BOEServer>  namebuffer       = new HashMap<String, BOEServer>();

  public BOEServers() {}

  public Iterator<BOEServer> iterator() {
    return this.servers.iterator();
  }


  public void remove(BOEServers removedservers) {
    for (final BOEServer removedserver : removedservers) {
      remove(removedserver);
    }
  }


  public void remove(BOEServer removedserver) {
    final List<Integer> removedPositions = new ArrayList<Integer>();
    int i                = 0;
    for (final BOEServer server : this.servers) {
      if (server.equals(removedserver)) {
        removedPositions.add(i);
      }
      i++;
    }

    for (final Integer removedPosition : removedPositions) {
      this.servers.remove(removedPosition.intValue());
    }
  }


  public void add(BOEServer newserver) {
    for (final BOEServer server : this.servers) {
      if (server.equals(newserver)) {
        if ((Integer)server.getPID() == null) {
          final int npid = newserver.getPID();
          if ((Integer)npid != null) {
            server.setPID(npid);
          }
        }

        if (server.getSIA() == null) {
            final String nSIA = newserver.getSIA();
            if (nSIA != null) {
              server.setSIA(nSIA);
            }
        }
        
        if (server.getFriendlyName() == null) {
            final String nFriendlyName = newserver.getFriendlyName();
            if (nFriendlyName != null) {
              server.setFriendlyName(nFriendlyName);
            }
        }

        if (server.getStatus() == null) {
            final String nStatus = newserver.getStatus();
            if (nStatus != null) {
              server.setStatus(nStatus);
            }
        }
        
        if ((Boolean)server.getDisabled() == null) {
            final boolean ndisabled = newserver.getDisabled();
            if ((Boolean) ndisabled != null) {
              server.setDisabled(ndisabled);
            }
        }
        
        if ((Date)server.getDate() == null) {
            final Date ncal = newserver.getDate();
            if ((Date) ncal != null) {
              server.setDate(ncal);
            }
        }

        return;
      }
    }

    if (newserver.getId() != -1) {
      this.idbuffer.put(newserver.getId(), newserver);
    }

    if (newserver.getName() != null) {
      this.namebuffer.put(newserver.getName(), newserver);
    }

    this.servers.add(newserver);
  }


  public BOEServer get(Integer id) {
    if (this.idbuffer.containsKey(id)) {
      return this.idbuffer.get(id);
    }

    for (final BOEServer server : this.servers) {
      if (server.getId() == id) {
        this.idbuffer.put(id, server);

        return server;
      }
    }

    return null;
  }


  public BOEServer get(String name) {
    if (this.namebuffer.containsKey(name)) {
      return this.namebuffer.get(name);
    }

    for (final BOEServer server : this.servers) {
      if (name.equals(server.getName())) {
        this.namebuffer.put(name, server);

        return server;
      }
    }

    return null;
  }


  public boolean contains(Integer id) {
    if (this.idbuffer.containsKey(id)) {
      return true;
    }

    for (final BOEServer _server : this.servers) {
      if (_server.getId() == id) {
        this.idbuffer.put(id, _server);

        return true;
      }
    }

    return false;
  }


  public boolean contains(String name) {
    if (name == null) {
      return false;
    }

    if (this.namebuffer.containsKey(name)) {
      return true;
    }

    for (final BOEServer _server : this.servers) {
      if (name.equals(_server.getName())) {
        this.namebuffer.put(name, _server);

        return true;
      }
    }

    return false;
  }


  public boolean contains(BOEServer server) {
    final String name = server.getName();

    return contains(name);
  }


  public String toString() {
    return this.servers.toString();
  }
  
  public ArrayList<String> getTableDef(){
	  ArrayList<String> listOfArrays=new ArrayList<String>();
	  listOfArrays.add("TBL_SERVERS");
	  listOfArrays.add("si_date|TIMESTAMP| |NOT NULL");
	  listOfArrays.add("si_id|integer| |NOT NULL");
	  listOfArrays.add("Server_name|varchar|255|NOT NULL");
	  listOfArrays.add("Sstatus|varchar|255| ");
	  listOfArrays.add("Senabled|varchar|10| ");
	  listOfArrays.add("sia|varchar|255| ");
	  listOfArrays.add("Spid|integer| | ");
	  listOfArrays.add("kind|varchar|255| ");
	  listOfArrays.add("autoboot|varchar|10| ");
	  listOfArrays.add("si_cluster|varchar|255| " );
	  
	  return listOfArrays;
  }
  
  public String getSQLPK(){
	  return "CREATE UNIQUE INDEX ind_serverlist_pk on tbl_servers (si_date, si_name, si_cluster)";
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
