package br.com.tarefa.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {

	private static final long serialVersionUID = 4370546423538526316L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;
	
	@Column(nullable = false, length = 50)
	private String nome;
	
	@Column(unique = true, nullable = false, length = 50)
	private String nomeUsuario;
	
	@Column(nullable = false)
	private String senha;

	@Column(nullable = false)
	private LocalDateTime dataCriacao;
	
	@Column(nullable = false)
	private LocalDateTime dataAtualizacao;
	
	@OneToMany(mappedBy = "usuario", orphanRemoval = true)
	private List<Tarefa> tarefas;

}
