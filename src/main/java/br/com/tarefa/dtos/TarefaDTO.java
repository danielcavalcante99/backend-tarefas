package br.com.tarefa.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.hateoas.RepresentationModel;

import br.com.tarefa.entities.enums.StatusTarefa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@Getter
@Setter
@Builder
@With
@AllArgsConstructor
@NoArgsConstructor(staticName = "create")
public class TarefaDTO extends RepresentationModel<TarefaDTO>  implements Serializable {

	private static final long serialVersionUID = -870760242503017379L;
	
	private UUID id;
	private String titulo;
	private String descricao;
	private LocalDateTime dataCriacao;
	private LocalDateTime dataAtualizacao;
	private StatusTarefa status;
	private UUID usuarioId;
}
