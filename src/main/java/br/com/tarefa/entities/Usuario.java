package br.com.tarefa.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {
	
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
	
	@Default
	@OneToMany(mappedBy = "usuario", orphanRemoval = true)
	private List<Tarefa> tarefas = new ArrayList<>();

}
