package br.com.tarefa.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.com.tarefa.entities.enums.StatusTarefa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tarefas")
public class Tarefa implements Serializable {

	private static final long serialVersionUID = -4878504104191449400L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;
	
	@Column(unique = true, nullable = false, length = 50)
	private String titulo;
	
	@Column(nullable = false, length = 250)
	private String descricao;
	
	@Column(nullable = false)
	private LocalDateTime dataCriacao;
	
	@Column(nullable = false)
	private LocalDateTime dataAtualizacao;
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private StatusTarefa status;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

}