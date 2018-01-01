package org.kik.agile.companion;

public enum PageEnum {

	LOGIN("/login", "template/login.peb"), //
	HOME("/companion/home", "template/home.peb"), //
	MAD_SAD_GLAD("/companion/madsadglad", "template/madsadglad.peb"), //
	ROTI("/companion/roti", "template/roti.peb");

	private String path;
	private String template;

	private PageEnum(String path, String template) {
		this.path = path;
		this.template = template;
	}

	public String getPath() {
		return path;
	}

	public String getTemplate() {
		return template;
	}

}
