package com.teambind.profileserver.events.event;

public abstract class Event {
	String topic;
	
	public Event(String topic) {
		this.topic = topic;
	}
	
	public String getTopic() {
		return topic;
	}
}
