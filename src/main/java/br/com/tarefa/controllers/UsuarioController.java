package br.com.tarefa.controllers;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
	           description = "Cria um novo usuário com base nos dados fornecidos. "
							+"Este endpoint verifica se o nome de usuário já está em uso antes de criar um novo registro. "
							+"Caso o nome de usuário já exista, não será permitida a operação. "
							+"Além disso, o método criptografa a senha do usuário antes de inserir na base de dados.")
	@ApiResponse(responseCode = "201", description = "Cadastro realizado com sucesso",
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "400", description = "Requisição inválida", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), 
		mediaType = MediaType.APPLICATION_JSON_VALUE))
	public ResponseEntity<UsuarioDTO> criarUsuario(@RequestBody CriarUsuarioDTO dto) {
		log.info("Requisição recebida para criar usuário: {}", dto.getNomeUsuario());	
		Usuario usuario = this.service.criarUsuario(dto);
		UsuarioDTO UsuarioDTO = this.mapper.usuarioToUsuarioDTO(usuario);
		return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioDTO);
	}

	@PutMapping("/atualizar")
	@Operation(summary = "Atualização de usuário",
			   description = "Atualiza as informações de um usuário com base nos dados fornecidos. "
							 +"Este endpoint busca o usuário atualmente logado e verifica se o nome de usuário "
							 +"a ser atualizado já está em uso por outro usuário. Sendo proibido a atualização caso o nome de usuário "
							 +"já exista e pertença a um usuário diferente. "
							 +"As informações do usuário, como nome, nome de usuário e senha, são atualizadas "
							 +"com base nos dados fornecidos na requisição. A senha é criptografada "
							 +"utilizando o BCryptPasswordEncoder antes de ser armazenada. "
							 +"Se todas as verificações forem bem-sucedidas, os dados do usuário na base de dados serão atualizados "
							 +"e o cache correspondente ao usuário será atualizado.",
			   security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponse(responseCode = "201", description = "Atualização realizada com sucesso", 
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "400", description = "Requisição inválida", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), 
		mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "401", description = "Não autenticado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	@ApiResponse(responseCode = "403", description = "Não autorizado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	@ApiResponse(responseCode = "404", description = "Recurso não encotrado", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), 
		mediaType = MediaType.APPLICATION_JSON_VALUE))
	public ResponseEntity<UsuarioDTO> atualizarUsuario(@RequestBody AtualizarUsuarioDTO dto) {
		log.info("Requisição recebida para atualizar usuário: {}", dto.getNomeUsuario());
		Usuario usuario = this.service.atualizarUsuario(dto);
		UsuarioDTO UsuarioDTO = this.mapper.usuarioToUsuarioDTO(usuario);
		return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioDTO);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Excluir usuário pelo id", 
			   description = "Permite excluir o usuário correspondente ao ID especificado."
						    +"Nesse serviço é verificado se o usuário existe. Somente o próprio usuário pode se excluir," 
						    +"sendo proibido a exclusão de outro usuário que seja diferente do seu. "
							+"Se todas as verificações forem bem-sucedidas, exclui o usuário da base de dados e "
						    +"remove o usuário do cache.",
			   security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponse(responseCode = "204", description = "Exclusão realizado com sucesso")
	@ApiResponse(responseCode = "400", description = "Requisição inválida", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), 
		mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "401", description = "Não autenticado", 
	    content = @Content(schema = @Schema(defaultValue = "")))
	@ApiResponse(responseCode = "403", description = "Não autorizado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	@ApiResponse(responseCode = "404", description = "Recurso não encotrado", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), mediaType = MediaType.APPLICATION_JSON_VALUE))
	public ResponseEntity<Void> excluirTarefaPeloId(@PathVariable UUID id) {
		log.info("Requisição recebida para excluir usuário pelo id: {}", id);
		this.service.excluirUsuarioPeloId(id);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/{id}")
	@Operation(summary = "Consultar usuário pelo id", 
			   description = "Permite consultar o usuário correspondente ao ID especificado."
							 +"* Além disso, o verifica se o nome de usuário do usuário encontrado corresponde ao usuário "
							 +"* atualmente logado. Se não corresponder, será proibido a visualização indicando que apenas o "
							 +"usuário pode visualizar seus próprios dados. "
							 +"O resultado da busca é armazenado em cache com a chave igual ao ID do usuário. "
							 +"*Se o resultado for null, ele não será armazenado no cache.",
			   security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponse(responseCode = "204", description = "Exclusão realizado com sucesso")
	@ApiResponse(responseCode = "400", description = "Requisição inválida", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), 
		mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "401", description = "Não autenticado", 
    	content = @Content(schema = @Schema(defaultValue = "")))
	@ApiResponse(responseCode = "403", description = "Não autorizado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	@ApiResponse(responseCode = "404", description = "Recurso não encotrado", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), mediaType = MediaType.APPLICATION_JSON_VALUE))
	public ResponseEntity<UsuarioDTO> buscarPeloId(@PathVariable UUID id) {
		Usuario usuario = this.service.buscarPeloId(id);
		UsuarioDTO usuarioDTO = this.mapper.usuarioToUsuarioDTO(usuario);
		Optional<UsuarioDTO> optUsuarioDto = Optional.ofNullable(usuarioDTO);
		
		return optUsuarioDto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
	}

}
