package com.ricston.contacts.app;

import org.glassfish.jersey.server.ResourceConfig;

import com.ricston.contacts.rest.features.DSFeature;
import com.ricston.contacts.rest.impl.ContactsResource;

public class ContactsApp extends ResourceConfig {

	public ContactsApp() {
		register(ContactsResource.class);
		register(DSFeature.class);
	}
}
