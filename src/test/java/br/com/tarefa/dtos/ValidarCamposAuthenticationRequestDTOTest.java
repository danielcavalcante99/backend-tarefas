package br.com.tarefa.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.tarefa.dtos.security.AuthenticationRequestDTO;

class ValidarCamposAuthenticationRequestDTOTest {

	private Validator validator;

	@BeforeEach
	void init() {
		this.validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	void testeVerficarParametrosNulosAuthenticationRequestDTO() {
		AuthenticationRequestDTO dto = new AuthenticationRequestDTO();
		Set<ConstraintViolation<AuthenticationRequestDTO>> violations = this.validator.validate(dto);

		violations.forEach(action -> {
			switch (action.getPropertyPath().toString()) {
			case "username":
				assertThat(action.getMessageTemplate()).isEqualTo("Campo 'username' é obrigatório");
				assertThat(action.getPropertyPath().toString()).isEqualTo("username");
				break;
			case "password":
				assertThat(action.getMessageTemplate()).isEqualTo("Campo 'password' é obrigatório");
				assertThat(action.getPropertyPath().toString()).isEqualTo("password");
				break;	
			default:
				break;
			}
		});

		assertThat(violations.stream().count()).isEqualTo(2);
	}

	@Test
	void testeVerificarTamanhoParametrosAuthenticationRequestDTO() {
		AuthenticationRequestDTO dto = createAuthenticationRequestDTO();

		Set<ConstraintViolation<AuthenticationRequestDTO>> violations = this.validator.validate(dto);

		violations.forEach(action -> {
			switch (action.getPropertyPath().toString()) {
			case "username":
				assertThat(action.getMessageTemplate())
						.isEqualTo("O campo 'username' é permitido um máximo de 50 caracteres");
				assertThat(action.getPropertyPath().toString()).isEqualTo("username");
				break;
			case "password":
				assertThat(action.getMessageTemplate())
						.isEqualTo("O campo 'password' é deve conter no mínimo 8 e no máximo de 11 caracteres");
				assertThat(action.getPropertyPath().toString()).isEqualTo("password");
				break;
			default:
				break;
			}
		});

		assertThat(violations.stream().count()).isEqualTo(2);
	}

	private AuthenticationRequestDTO createAuthenticationRequestDTO() {
		return AuthenticationRequestDTO.builder()
				.username(RandomStringUtils.randomAlphanumeric(51))
				.password(RandomStringUtils.randomAlphanumeric(12))
				.build();
	}
}
