package br.com.tarefa.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidarCamposCriarUsuarioDTOTest {

	private Validator validator;

	@BeforeEach
	void init() {
		this.validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	void testeVerificarParametrosNulosCriarUsuarioDTO() {
		CriarUsuarioDTO dto = new CriarUsuarioDTO();
		Set<ConstraintViolation<CriarUsuarioDTO>> violations = this.validator.validate(dto);

		violations.forEach(action -> {
			switch (action.getPropertyPath().toString()) {
			case "nome":
				assertThat(action.getMessageTemplate()).isEqualTo("Campo 'nome' é obrigatório");
				assertThat(action.getPropertyPath().toString()).isEqualTo("nome");
				break;
			case "nomeUsuario":
				assertThat(action.getMessageTemplate()).isEqualTo("Campo 'nomeUsuario' é obrigatório");
				assertThat(action.getPropertyPath().toString()).isEqualTo("nomeUsuario");
				break;
			case "senha":
				assertThat(action.getMessageTemplate()).isEqualTo("Campo 'senha' é obrigatório");
				assertThat(action.getPropertyPath().toString()).isEqualTo("senha");
				break;
			default:
				break;
			}
		});

		assertThat(violations.stream().count()).isEqualTo(3);
	}

	@Test
	void testeVerificarTamanhoFormatoParametrosCriarUsuarioDTO() {
		CriarUsuarioDTO dto = createCriarUsuarioDTO();

		Set<ConstraintViolation<CriarUsuarioDTO>> violations = this.validator.validate(dto);

		violations.forEach(action -> {
			switch (action.getPropertyPath().toString()) {
			case "nome":
				assertThat(action.getMessageTemplate())
						.isEqualTo("O campo 'nome' é permitido um máximo de 50 caracteres");
				assertThat(action.getPropertyPath().toString()).isEqualTo("nome");
				break;
			case "nomeUsuario":
				assertThat(action.getMessageTemplate())
						.isEqualTo("O campo 'nomeUsuario' é permitido um máximo de 50 caracteres");
				assertThat(action.getPropertyPath().toString()).isEqualTo("nomeUsuario");
				break;	
			case "senha":
				assertThat(action.getMessageTemplate())
						.isEqualTo("O campo 'senha' é deve conter no mínimo 8 e no máximo de 11 caracteres");
				assertThat(action.getPropertyPath().toString()).isEqualTo("senha");
				break;
			default:
				break;
			}
		});

		assertThat(violations.stream().count()).isEqualTo(3);
	}

	private CriarUsuarioDTO createCriarUsuarioDTO() {
		return CriarUsuarioDTO.builder()
				.nome(RandomStringUtils.randomAlphanumeric(51))
				.nomeUsuario(RandomStringUtils.randomAlphanumeric(51))
				.senha(RandomStringUtils.randomAlphanumeric(7)).build();
	}
	
}
