package rit.eyeTracking;

import java.io.Externalizable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EventImpl implements Event, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6719375361854591315L;
	private final ID id;
	private final Map<String,Object> attributes;
	
	public EventImpl(ID id, Map<String,Object> initialAttributes) {
		this.id = id;
		this.attributes = initialAttributes;
	}

	@Override
	public ID getID() {
		return id;
	}
	
	@Override
	public void addAttribute(String name, Object value) {
		attributes.put(name, value);
	}
	
	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}

	@Override
	public void clear() {
		attributes.clear();
	}

	@Override
	public String toString() {
		return id.toString()+" "+attributes.toString();
	}

	@Override
	public Event getSerializable() {
		Map<String,Object> attr = new HashMap<String,Object>(attributes.size());
		EventImpl copy = new EventImpl(id, attr);
		for(String key: attributes.keySet()) {
			Object value = attributes.get(key);
			if(value instanceof Serializable
				|| value instanceof Externalizable) {
				attr.put(key, value);
			}
		}
		return copy;
	}

	@Override
	public int hashCode() {
		return id.hashCode() | attributes.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null
				&& obj instanceof Event
				&& ((Event)obj).getID().equals(id)
				&& ((Event)obj).getAttributes().equals(attributes);
	}
	
	

}
