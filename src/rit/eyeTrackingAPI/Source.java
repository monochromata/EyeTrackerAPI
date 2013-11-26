package rit.eyeTrackingAPI;

/**
 * 
 */
public interface Source {
	
	/**
	 * Return the names of attributes that this source adds to events
	 * it creates. Not that not all events created by this source will
	 * actually have instance of these attributes.
	 * 
	 * @return An array of attribute names
	 */
	public String[] getAttributesCreated();
}
