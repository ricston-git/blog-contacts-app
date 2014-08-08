package com.ricston.contacts.ds;

import java.util.ArrayList;
import java.util.List;

import com.ricston.blog.contactsapp.rest.api.model.ApiContact;
import com.ricston.blog.contactsapp.rest.api.model.ApiContacts;
import com.ricston.contacts.domain.Contact;

public class DataStore {

	List<Contact> contactList;

	public DataStore() {
		contactList = new ArrayList<Contact>();
	}

	public ApiContacts getContacts() {
		ApiContacts apiContacts = new ApiContacts();
		List<ApiContact> apiContactList = new ArrayList<ApiContact>();
		for (Contact c : contactList) {
			ApiContact apiContact = new ApiContact();
			apiContact.setId("" + c.getId());
			apiContact.setName(c.getName());
			apiContact.setEmail(c.getEmail());

			apiContactList.add(apiContact);
		}
		apiContacts.setSize(apiContactList.size());
		apiContacts.setContacts(apiContactList);
		return apiContacts;
	}
	
	public Long saveContact(Contact contact) {
		Long id = new Long(contactList.size() + 1);
		contact.setId(id);
		contactList.add(contact);

		return id;
	}

	public Contact getContactById(Long id) {
		if (id == null)
			return null;
		for (Contact c : contactList) {
			if (c.getId().equals(id)) {
				return c;
			}
		}

		return null;
	}
}
