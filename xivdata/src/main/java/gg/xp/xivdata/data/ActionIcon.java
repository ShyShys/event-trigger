package gg.xp.xivdata.data;

import java.net.URL;

public class ActionIcon implements HasIconURL {


	private final URL url;


	ActionIcon(URL url) {
		this.url = url;
	}

	@Override
	public URL getIconUrl() {
		return url;
	}
}
