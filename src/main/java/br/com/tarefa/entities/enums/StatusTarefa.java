package br.com.tarefa.entities.enums;

public enum StatusTarefa {

	PENDENTE("P", "Pendente"), 
	ANDAMENTO("A", "Andamento"), 
	CONCLUIDA("C", "Concluída");

	private String value;
	private String description;

	private StatusTarefa(String value, String description) {
		this.value = value;
		this.description = description;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
