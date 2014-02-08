package rit.eyeTrackingAPI;

import java.util.Collections;
import java.util.Map;

public class EventImpl implements Event {
	
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

}
