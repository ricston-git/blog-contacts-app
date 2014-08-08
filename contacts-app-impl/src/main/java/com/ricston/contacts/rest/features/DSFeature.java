package com.ricston.contacts.rest.features;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.ricston.contacts.ds.DataStore;

public class DSFeature implements Feature {

	@Override
	public boolean configure(FeatureContext context) {
		context.register(new DSBinder());
		return true;
	}
	
	public static class DSBinder extends AbstractBinder {

		@Override
		protected void configure() {
			bind(new DataStore()).to(DataStore.class);
		}
		
	}

}
