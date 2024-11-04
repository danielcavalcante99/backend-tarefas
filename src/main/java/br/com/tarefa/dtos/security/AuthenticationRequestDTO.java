package br.com.tarefa.dtos.security;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequestDTO implements Serializable{

	private static final long serialVersionUID = 124619630016458852L;
	
	@NotBlank(message = "Campo 'username' é obrigatório")
	@Size(max = 50, message = "O campo 'username' é permitido um máximo de 50 caracteres")
	private String username;
	
	@NotBlank(message = "Campo 'password' é obrigatório")
	@Size(min = 8, max = 11, message = "O campo 'password' é deve conter no mínimo 8 e no máximo de 11 caracteres")
	private String password;

}