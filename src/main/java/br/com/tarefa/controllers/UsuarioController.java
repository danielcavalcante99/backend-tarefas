package br.com.tarefa.controllers;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.tarefa.dtos.AtualizarUsuarioDTO;
import br.com.tarefa.dtos.CriarUsuarioDTO;
import br.com.tarefa.dtos.UsuarioDTO;
import br.com.tarefa.entities.Usuario;
import br.com.tarefa.exceptions.handlers.ApiRequestException;
import br.com.tarefa.mappers.UsuarioMapper;
import br.com.tarefa.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Usuários")
@RestController
@RequestMapping("/v1/usuarios")
public class UsuarioController {
	
	private final UsuarioMapper mapper;
	private final UsuarioService service;
	 
	public UsuarioController(UsuarioMapper mapper, UsuarioService service) {
		this.mapper = mapper;
		this.service = service;
	}

	@PostMapping("/criar")
	@Operation(summary = "Cadastro de usuário", 
	           description = "Realizar cadastro de usuário")
	@ApiResponse(responseCode = "201", description = "Cadastro realizado com sucesso", 
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "400", description = "Requisição inválida", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), 
		mediaType = MediaType.APPLICATION_JSON_VALUE))
	public ResponseEntity<UsuarioDTO> criarUsuario(@RequestBody CriarUsuarioDTO dto) {
		log.info("Requisição recebida para criar usuário: {}", dto.getNomeUsuario());	
		Usuario usuario = this.service.criarUsuario(dto);
		UsuarioDTO UsuarioDTO = this.mapper.UsuarioToUsuarioDTO(usuario);
		return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioDTO);
	}

	@PutMapping("/atualizar")
	@Operation(summary = "Atualização de usuário",
			   description = "Permite que o usuário logado atualize apenas suas próprias informações. A atualização de dados de outros usuários não será permitida",
			   security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponse(responseCode = "201", description = "Atualização realizada com sucesso", 
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "404", description = "Recurso não encotrado", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), 
		mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "400", description = "Requisição inválida", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), 
		mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "403", description = "Não autorizado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	@ApiResponse(responseCode = "401", description = "Não autenticado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	public ResponseEntity<UsuarioDTO> atualizarUsuario(@RequestBody AtualizarUsuarioDTO dto) {
		log.info("Requisição recebida para atualizar usuário: {}", dto.getNomeUsuario());
		Usuario usuario = this.service.atualizarUsuario(dto);
		UsuarioDTO UsuarioDTO = this.mapper.UsuarioToUsuarioDTO(usuario);
		return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioDTO);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Excluir usuário pelo id", 
			   description = "Permite que o usuário logado exclua seu próprio usuário",
			   security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponse(responseCode = "204", description = "Exclusão realizado com sucesso")
	@ApiResponse(responseCode = "404", description = "Recurso não encotrado", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "403", description = "Não autorizado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	@ApiResponse(responseCode = "401", description = "Não autenticado", 
	    content = @Content(schema = @Schema(defaultValue = "")))
	public ResponseEntity<Void> excluirTarefaPeloId(@PathVariable UUID id) {
		log.info("Requisição recebida para excluir usuário pelo id: {}", id);
		this.service.excluirTarefaPeloId(id);
		return ResponseEntity.noContent().build();
	}

}
