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
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CriarUsuarioDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@NotBlank(message = "Campo 'nome' é obrigatório")
	@Size(max = 50, message = "O campo 'nome' é permitido um máximo de 50 caracteres")
	private String nome;
	
	@NotBlank(message = "Campo 'nomeUsuario' é obrigatório")
	@Size(max = 50, message = "O campo 'nomeUsuario' é permitido um máximo de 50 caracteres")
	private String nomeUsuario;
	
	@NotBlank(message = "Campo 'senha' é obrigatório")
	@Size(min = 8, max = 11, message = "O campo 'senha' é deve conter no mínimo 8 e no máximo de 11 caracteres")
	private String senha;

}
