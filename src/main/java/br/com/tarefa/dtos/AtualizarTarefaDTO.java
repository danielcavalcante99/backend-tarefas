package br.com.tarefa.dtos;

import java.io.Serializable;
import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
public class AtualizarTarefaDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@NotNull(message = "Campo 'id' é obrigatório")
	private UUID id;
	
	@Size(max = 50, message = "O campo 'titulo' é permitido um máximo de 50 caracteres")
	@NotBlank(message = "Campo 'titulo' é obrigatório")
	private String titulo;
	
	@Size(max = 250, message = "O campo 'descricao' é permitido um máximo de 250 caracteres")
	@NotBlank(message = "Campo 'descricao' é obrigatório")
	private String descricao;
	
	@NotNull(message = "Campo 'status' é obrigatório")
	private StatusTarefa status;

}
