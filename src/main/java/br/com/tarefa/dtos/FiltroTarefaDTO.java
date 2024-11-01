package br.com.tarefa.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.validation.constraints.Size;

import br.com.tarefa.entities.enums.StatusTarefa;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
public class FiltroTarefaDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private UUID id;
	
	@Size(max = 50, message = "O campo 'titulo' é permitido um máximo de 50 caracteres")
	private String titulo;
	
	@Size(max = 250, message = "O campo 'descricao' é permitido um máximo de 250 caracteres")
	private String descricao;

	private StatusTarefa status;
	
	private UUID usuarioId;
	
	private LocalDateTime dataCriacaoInicio;
	
	private LocalDateTime dataCriacaoFim;
	
	private LocalDateTime dataAtualizacaoInicio;
	
	private LocalDateTime dataAtualizacaoFim;

}
