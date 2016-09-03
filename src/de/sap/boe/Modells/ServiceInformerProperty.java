package de.sap.boe.Modells;

@SuppressWarnings("unchecked")
public class ServiceInformerProperty implements Comparable {

	  private String   name;
	  private Object   value;
	  private String   Objecttype;
	  


	  public ServiceInformerProperty (String name) {
		    this.name = name;
		  }  

	  public ServiceInformerProperty (String name, Object value, String Objecttype) {
		    this.name = name;
		    this.value = value;
		    this.Objecttype = Objecttype;
		  }  
	  

	  public int compareTo(Object other) throws ClassCastException {
		    if (!(other instanceof BOEServer)) {
		      throw new ClassCastException("Cannot compare apples with oranges.");
		    }

		    return this.name.compareTo(((ServiceInformerProperty) other).getName());
		  }
	


// Name
	  public void setName(String name) {
		    this.name = name;
		  }


	  public String getName() {
		    return this.name;
		  }

// value
	  public void setValue(Object value) {
		    this.value = value;
		  }


	  public Object getValue() {
		    return this.value;
		  }

	  
// Objecttype
	  public void setObjecttype(String Objecttype) {
		    this.Objecttype = Objecttype;
		  }


	  public String getObjecttype() {
		    return this.Objecttype;
		  }

	  
	  public String toString() {
		  return "[" + this.name  + "=" + this.value + "]";
	  }
	  
}

