package rit.eyeTracking;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.HashMap;
import java.util.Map;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import rit.eyeTracking.Event.ID;

public class AbstractEventTest {

	@Rule
	public JUnitRuleMockery context = new JUnitRuleMockery();

	@SuppressWarnings({ "serial", "unchecked", "rawtypes" })
	@Test
	public void testAccessors() {
		
		final ID id = context.mock(ID.class);
		final Map attributes = context.mock(Map.class);
		final String attribute1Value = "attribute1Value";
		
		Expectations expectations = new Expectations() {{
			exactly(1).of(attributes).get("attribute0Name");
				will(returnValue("attribute0Value"));
			
			exactly(1).of(attributes).put("attribute1Name", "attribute1Value");
			exactly(1).of(attributes).get("attribute1Name");
				will(returnValue(attribute1Value));
		}};
		
		context.checking(expectations);
		
		AbstractEvent event = new AbstractEvent(id, attributes) {
			@Override
			public boolean isNew() {
				return false;
			}};
		
		assertThat(event, is(notNullValue()));
		assertThat(event.getID(), is(sameInstance(id)));
		assertThat(event.getAttributes(), is(not(sameInstance(attributes))));
		try {
			event.getAttributes().put("", "");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException uoe) {
			assertThat(uoe, is(notNullValue()));
		}
		assertThat(event.getAttribute("attribute0Name"), is("attribute0Value"));
		event.addAttribute("attribute1Name", attribute1Value);
		assertThat(event.getAttribute("attribute1Name"), is(sameInstance(attribute1Value)));
	}
	
	
	@SuppressWarnings({ "serial", "unchecked", "rawtypes" })
	@Test
	public void testClear() {
		
		final ID id = context.mock(ID.class);
		final Map attributes = context.mock(Map.class);
		
		Expectations expectations = new Expectations() {{
			exactly(1).of(attributes).clear();
		}};
		
		context.checking(expectations);
		
		AbstractEvent event = new AbstractEvent(id, attributes) {
			@Override
			public boolean isNew() {
				return false;
			}};
		
		assertThat(event, is(notNullValue()));
		assertThat(event.getID(), is(sameInstance(id)));
		event.clear();
		assertThat(event.getID(), is(sameInstance(id)));
	}
	
	@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
	@Test
	public void testGetSerializable() {
		
		final ID id = context.mock(ID.class);
		final EventFactory eventFactory= context.mock(EventFactory.class);
		final Object nonSerializable = new Object();
		final Integer serializable= new Integer(1);
		final Map attributes = new HashMap();
		attributes.put("nonSerializable", nonSerializable);
		attributes.put("serializable", serializable);
		
		final Event serializableEventMock= context.mock(Event.class);
		
		Expectations expectations = new Expectations() {{
			exactly(1).of(eventFactory).createEvent(id, new HashMap());
				will(returnValue(serializableEventMock));
				
			exactly(1).of(serializableEventMock).addAttribute("serializable", serializable);
			exactly(1).of(serializableEventMock).getID();
				will(returnValue(id));
		}};
		
		context.checking(expectations);
		
		AbstractEvent event = new AbstractEvent(id, attributes) {
			@Override
			public boolean isNew() {
				return false;
			}};
		
		assertThat(event, is(notNullValue()));
		assertThat(event.getAttributes().size(), is(2));
		assertThat(event.getAttribute("nonSerializable"), is(sameInstance(nonSerializable)));
		assertThat(event.getAttribute("serializable"), is(sameInstance(serializable)));
		
		Event serializableEvent= event.getSerializable(eventFactory);
		assertThat(serializableEvent, is(notNullValue()));
		assertThat(serializableEvent.getID(), is(sameInstance(id)));
	}

	@Test
	public void testEqualsContract() {
		EqualsVerifier
			.forClass(AbstractEvent.class)
			.usingGetClass()
			.verify();
	}

	@Test
	public void testToString() {
		
		final ID id = context.mock(ID.class);
		final Map attributes = context.mock(Map.class);
		
		Expectations expectations = new Expectations() {{
		}};
		
		context.checking(expectations);
		
		AbstractEvent event = new AbstractEvent(id, attributes) {
			@Override
			public boolean isNew() {
				return false;
			}};
		
		assertThat(event, is(notNullValue()));
		assertThat(event.toString(), is(id.toString()+" "+attributes.toString()));
	}

}
