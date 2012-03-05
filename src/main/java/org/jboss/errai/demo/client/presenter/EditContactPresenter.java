package org.jboss.errai.demo.client.presenter;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.demo.client.event.ContactUpdatedEvent;
import org.jboss.errai.demo.client.event.EditContactCancelledEvent;
import org.jboss.errai.demo.shared.Contact;
import org.jboss.errai.demo.shared.ContactsService;
import org.jboss.errai.ioc.client.api.Caller;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

@Dependent
public class EditContactPresenter implements Presenter {
	public interface Display {
		HasClickHandlers getSaveButton();
		HasClickHandlers getCancelButton();
		HasValue<String> getFirstName();
		HasValue<String> getLastName();
		HasValue<String> getEmailAddress();
		Widget asWidget();
	}

	private Contact contact;
	
	@Inject
	private Caller<ContactsService> contactsService;

	@Inject
	private HandlerManager eventBus;
	
	@Inject
	private Display display;

	public EditContactPresenter() {
		this.contact = new Contact();
	}

	public EditContactPresenter(HandlerManager eventBus, Display display,
			String id) {
		this.eventBus = eventBus;
		this.display = display;

		contactsService.call(new RemoteCallback<Contact>() {
			public void callback(Contact result) {
				contact = result;
				EditContactPresenter.this.display.getFirstName().setValue(
						contact.getFirstName());
				EditContactPresenter.this.display.getLastName().setValue(
						contact.getLastName());
				EditContactPresenter.this.display.getEmailAddress().setValue(
						contact.getEmailAddress());
			}
		}).getContact(id);

	}

	public void bind() {
		this.display.getSaveButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				doSave();
			}
		});

		this.display.getCancelButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				eventBus.fireEvent(new EditContactCancelledEvent());
			}
		});
	}

	public void go(final HasWidgets container) {
	    bind();
		container.clear();
		container.add(display.asWidget());
	}

	private void doSave() {
		contact.setFirstName(display.getFirstName().getValue());
		contact.setLastName(display.getLastName().getValue());
		contact.setEmailAddress(display.getEmailAddress().getValue());

		contactsService.call(new RemoteCallback<Contact>() {
			public void callback(Contact result) {
				eventBus.fireEvent(new ContactUpdatedEvent(result));
			}
		}).updateContact(contact);
	}
}