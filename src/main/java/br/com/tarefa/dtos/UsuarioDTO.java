package br.com.tarefa.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

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
public class UsuarioDTO implements Serializable {
	
	private static final long serialVersionUID = 5133501006561968929L;
	
	private UUID id;
	private String nome;
	private String nomeUsuario;
	private String senha;
	private LocalDateTime dataCriacao;
	private LocalDateTime dataAtualizacao;

}
