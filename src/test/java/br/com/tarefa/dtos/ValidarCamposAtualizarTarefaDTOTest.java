package br.com.tarefa.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.tarefa.entities.enums.StatusTarefa;

class ValidarCamposAtualizarTarefaDTOTest {

	private Validator validator;

	@BeforeEach
	void init() {
		this.validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	void testeVerficarParametrosNulosCriarTarefaDTO() {
		AtualizarTarefaDTO dto = new AtualizarTarefaDTO();
		Set<ConstraintViolation<AtualizarTarefaDTO>> violations = this.validator.validate(dto);

		violations.forEach(action -> {
			switch (action.getPropertyPath().toString()) {
			case "id":
				assertThat(action.getMessageTemplate()).isEqualTo("Campo 'id' é obrigatório");
				assertThat(action.getPropertyPath().toString()).isEqualTo("id");
				break;
			case "titulo":
				assertThat(action.getMessageTemplate()).isEqualTo("Campo 'titulo' é obrigatório");
				assertThat(action.getPropertyPath().toString()).isEqualTo("titulo");
				break;	
			case "descricao":
				assertThat(action.getMessageTemplate()).isEqualTo("Campo 'descricao' é obrigatório");
				assertThat(action.getPropertyPath().toString()).isEqualTo("descricao");
				break;
			case "status":
				assertThat(action.getMessageTemplate()).isEqualTo("Campo 'status' é obrigatório");
				assertThat(action.getPropertyPath().toString()).isEqualTo("status");
				break;	
			default:
				break;
			}
		});

		assertThat(violations.stream().count()).isEqualTo(4);
	}

	@Test
	void testeVerificarTamanhoParametrosCriarTarefaDTO() {
		AtualizarTarefaDTO dto = AtualizarTarefaDTO.builder()
				.id(UUID.randomUUID())
				.titulo(StringUtils.leftPad("a", 51))
				.descricao(StringUtils.leftPad("a", 251))
				.status(StatusTarefa.ANDAMENTO)
				.build();

		Set<ConstraintViolation<AtualizarTarefaDTO>> violations = this.validator.validate(dto);

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
}
