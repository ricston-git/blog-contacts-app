package com.ricston.contacts.rest.impl;

import javax.inject.Inject;

import com.ricston.blog.contactsapp.rest.api.model.ApiContact;
import com.ricston.blog.contactsapp.rest.api.model.ApiContacts;
import com.ricston.blog.contactsapp.rest.api.model.ApiCreatedEntity;
import com.ricston.blog.contactsapp.rest.api.resource.Contacts;
import com.ricston.contacts.domain.Contact;
import com.ricston.contacts.ds.DataStore;

public class ContactsResource implements Contacts {
	
	@Inject
	DataStore ds;

	@Override
	public GetContactsResponse getContacts() throws Exception {
		try {
			
			ApiContacts apiContacts = ds.getContacts();

			return GetContactsResponse.jsonOK(apiContacts);
		} catch (Exception e) {
			e.printStackTrace();
			return GetContactsResponse.internalServerError();
		}
	}

	@Override
	public GetContactsByIdResponse getContactsById(String idStr)
			throws Exception {

		try {

			Long id = Long.parseLong(idStr);
			Contact contact = ds.getContactById(id);

			ApiContact apiContact = new ApiContact();
			apiContact.setId("" + contact.getId());
			apiContact.setName(contact.getName());
			apiContact.setEmail(contact.getEmail());

			return GetContactsByIdResponse.jsonOK(apiContact);

		} catch (NumberFormatException e) {
			e.printStackTrace();
			return GetContactsByIdResponse.badRequest();
		} catch (Exception e) {
			e.printStackTrace();
			return GetContactsByIdResponse.internalServerError();
		}
	}

	@Override
	public PostContactsResponse postContacts(String name, String email)
			throws Exception {

		try {
			Contact contact = new Contact();
			contact.setName(name);
			contact.setEmail(email);

			Long id = ds.saveContact(contact);

			ApiCreatedEntity apiCreatedEntity = new ApiCreatedEntity();
			apiCreatedEntity.setId("" + id);

			return PostContactsResponse.jsonOK(apiCreatedEntity);
		} catch (Exception e) {
			e.printStackTrace();
			return PostContactsResponse
					.plainInternalServerError("Could not create new contact with name: "
							+ name + ", email: " + email);
		}
	}


}
