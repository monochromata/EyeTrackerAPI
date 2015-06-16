package rit.eyeTracking;

import java.io.Externalizable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEvent implements Event, Serializable {
	
	private static final long serialVersionUID = -6719375361854591315L;
	private final ID id;
	private final Map<String,Object> attributes;
	
	public AbstractEvent(ID id, Map<String,Object> initialAttributes) {
		this.id = id;
		this.attributes = initialAttributes;
	}

	@Override
	public ID getID() {
		return id;
	}
	
	@Override
	public void addAttribute(String name, Object value) {
		/*if(attributes.containsKey(name)) {
			System.err.println("AbstractEvent: overwriting old "+name+" value="+attributes.get(name)+" with "+value);
		}*/
		attributes.put(name, value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name) {
		return (T)attributes.get(name);
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
	public Event getSerializable(EventFactory factory) {
		Map<String,Object> attr = new HashMap<String,Object>(attributes.size());
		Event copy = factory.createEvent(id, attr);
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
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractEvent other = (AbstractEvent) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	

}
