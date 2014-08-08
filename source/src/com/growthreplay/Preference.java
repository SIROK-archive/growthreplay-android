package com.growthreplay;

import com.growthreplay.model.Client;

public class Preference extends com.growthbeat.Preference {

	private static final String FILE_NAME = "growthreplay-preferences";
	private static final String CLIENT_KEY = "client";

	public Preference() {
		super();
		setFileName(FILE_NAME);
	}

	public Client fetchClient() {

		Client client = new Client();
		client.setJsonObject(get(CLIENT_KEY));
		return client;

	}

	public synchronized void saveClient(Client client) {

		if (client == null)
			return;

		save(CLIENT_KEY, client.getJsonObject());

	}

}