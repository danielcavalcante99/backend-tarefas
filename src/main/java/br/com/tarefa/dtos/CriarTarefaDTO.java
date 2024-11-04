package br.com.tarefa.dtos;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CriarTarefaDTO implements Serializable {

	private static final long serialVersionUID = 1895243719994180822L;

	@Size(max = 50, message = "O campo 'titulo' é permitido um máximo de 50 caracteres")
	@NotBlank(message = "Campo 'titulo' é obrigatório")
	private String titulo;
	
	@Size(max = 250, message = "O campo 'descricao' é permitido um máximo de 250 caracteres")
	@NotBlank(message = "Campo 'descricao' é obrigatório")
	private String descricao;
	
}
