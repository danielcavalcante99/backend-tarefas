package br.com.tarefa.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidarCamposFiltroTarefaDTOTest {

	private Validator validator;

	@BeforeEach
	void init() {
		this.validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	void testeVerificarTamanhoParametrosFiltroTarefaDTO() {
		FiltroTarefaDTO dto = createFiltroTarefaDTO();

		Set<ConstraintViolation<FiltroTarefaDTO>> violations = this.validator.validate(dto);

		violations.forEach(action -> {
			switch (action.getPropertyPath().toString()) {
			case "titulo":
				assertThat(action.getMessageTemplate())
						.isEqualTo("O campo 'titulo' é permitido um máximo de 50 caracteres");
				assertThat(action.getPropertyPath().toString()).isEqualTo("titulo");
				break;
			case "descricao":
				assertThat(action.getMessageTemplate())
						.isEqualTo("O campo 'descricao' é permitido um máximo de 250 caracteres");
				assertThat(action.getPropertyPath().toString()).isEqualTo("descricao");
				break;
			default:
				break;
			}
		});

		assertThat(violations.stream().count()).isEqualTo(2);
	}

	private FiltroTarefaDTO createFiltroTarefaDTO() {
		return FiltroTarefaDTO.builder()
				.titulo(RandomStringUtils.randomAlphanumeric(51))
				.descricao(RandomStringUtils.randomAlphanumeric(251)).build();
	}
}
