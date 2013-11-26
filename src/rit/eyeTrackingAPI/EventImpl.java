package rit.eyeTrackingAPI;

import java.util.Collections;
import java.util.Map;

public class EventImpl implements Event {
	
	private final Map<String,Object> attributes;
	
	public EventImpl(Map<String,Object> initialAttributes) {
		this.attributes = initialAttributes;
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

}
