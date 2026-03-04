package com.addressbook.service;

import com.addressbook.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactManager {
  private final DataStorageService storageService;
  private final List<Contact> contacts;

  public ContactManager() {
    this.storageService = new DataStorageService();
    this.contacts = this.storageService.loadContacts();
  }

  public List<Contact> getAllContacts() {
    return contacts;
  }

  public void addContact(Contact contact) {
    contacts.add(contact);
    storageService.saveContacts(contacts);
  }

  public void updateContact(Contact contact) {
    // Since Contact objects are mutated directly (or replaced by index before
    // calling update),
    // we might just need to save. But let's find by ID.
    for (int i = 0; i < contacts.size(); i++) {
      if (contacts.get(i).getId().equals(contact.getId())) {
        contacts.set(i, contact);
        break;
      }
    }
    storageService.saveContacts(contacts);
  }

  public void deleteContact(Contact contact) {
    contacts.remove(contact);
    storageService.saveContacts(contacts);
  }
}
