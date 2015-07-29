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

			return GetContactsResponse.withJsonOK(apiContacts);
		} catch (Exception e) {
			e.printStackTrace();
			return GetContactsResponse.withInternalServerError();
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

			return GetContactsByIdResponse.withJsonOK(apiContact);

		} catch (NumberFormatException e) {
			e.printStackTrace();
			return GetContactsByIdResponse.withBadRequest();
		} catch (Exception e) {
			e.printStackTrace();
			return GetContactsByIdResponse.withInternalServerError();
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

			return PostContactsResponse.withJsonOK(apiCreatedEntity);
		} catch (Exception e) {
			e.printStackTrace();
			return PostContactsResponse
					.withPlainInternalServerError("Could not create new contact with name: "
							+ name + ", email: " + email);
		}
	}


}
