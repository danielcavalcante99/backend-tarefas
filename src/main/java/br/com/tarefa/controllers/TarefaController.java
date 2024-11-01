package br.com.tarefa.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.hateoas.IanaLinkRelations;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.tarefa.dtos.AtualizarTarefaDTO;
import br.com.tarefa.dtos.CriarTarefaDTO;
import br.com.tarefa.dtos.FiltroTarefaDTO;
import br.com.tarefa.dtos.TarefaDTO;
import br.com.tarefa.entities.Tarefa;
import br.com.tarefa.entities.enums.StatusTarefa;
import br.com.tarefa.exceptions.handlers.ApiRequestException;
import br.com.tarefa.mappers.TarefaMapper;
import br.com.tarefa.services.TarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Tarefas")
@RestController
@RequestMapping("/v1/tarefas")
public class TarefaController {

	private final TarefaMapper mapper;
	private final TarefaService service;
	
	public TarefaController(TarefaMapper mapper, TarefaService service) {
        this.mapper = mapper;
        this.service = service;
	}   
	
	@PostMapping("/criar")
	@Operation(summary = "Cadastro de tarefa", 
	           description = "Permite o cadastro de tarefas, mas não será possível criar uma nova tarefa com um título que já exista",
	        	security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponse(responseCode = "201", description = "Cadastro realizado com sucesso", 
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "400", description = "Requisição inválida", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), 
		mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "401", description = "Não autenticado", 
	    content = @Content(schema = @Schema(defaultValue = "")))
	public ResponseEntity<TarefaDTO> criarTarefa(@RequestBody CriarTarefaDTO dto) {
		log.info("Requisição recebida para criar tarefa: {}", dto.getTitulo());
		Tarefa tarefa = this.service.criarTarefa(dto);
		TarefaDTO tarefaDTO = this.mapper.tarefaToTarefaDTO(tarefa);
		return ResponseEntity.status(HttpStatus.CREATED).body(tarefaDTO);
	}

	@PutMapping("/atualizar")
	@Operation(summary = "Atualização de usuário",
			   description = "Permite o usuário logado atualizar apenas suas próprias tarefas",
			   security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponse(responseCode = "201", description = "Atualização realizada com sucesso", 
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "404", description = "Recurso não encotrado", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), 
		mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "403", description = "Não autorizado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	@ApiResponse(responseCode = "401", description = "Não autenticado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	public ResponseEntity<TarefaDTO> atualizarTarefa(@RequestBody AtualizarTarefaDTO dto) {
		log.info("Requisição recebida para atualizar tarefa: {}", dto.getTitulo());
		Tarefa tarefa = this.service.atualizarTarefa(dto);
		TarefaDTO tarefaDTO = this.mapper.tarefaToTarefaDTO(tarefa);
		return ResponseEntity.status(HttpStatus.CREATED).body(tarefaDTO);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Excluir usuário pelo id", 
			   description = "Permite que o usuário logado exclua apenas suas tarefas pelo id",
			   security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponse(responseCode = "204", description = "Exclusão realizado com sucesso")
	@ApiResponse(responseCode = "404", description = "Recurso não encotrado", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "403", description = "Não autorizado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	@ApiResponse(responseCode = "401", description = "Não autenticado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	public ResponseEntity<Void> excluirTarefaPeloId(@PathVariable UUID id) {
		log.info("Requisição recebida para excluir tarefa pelo id: {}", id);
		this.service.excluirTarefaPeloId(id);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/{id}")
	@Operation(summary = "Consultar tarefa pelo id", 
			   description = "Permite consultar tarefa pelo id",
	           security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponse(responseCode = "200", description = "Busca realizada com sucesso", 
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "204", description = "Sem conteúdo")
	@ApiResponse(responseCode = "401", description = "Não autenticado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	public ResponseEntity<TarefaDTO> buscarPeloId(@PathVariable UUID id) {
		Tarefa tarefa = this.service.buscarPeloId(id);
		TarefaDTO tarefaDTO = this.mapper.tarefaToTarefaDTO(tarefa);
		tarefaDTO.add(linkTo(methodOn(TarefaController.class).listarTarefas()).withRel(IanaLinkRelations.COLLECTION));
			
		Optional<TarefaDTO> optTarefaDto = Optional.ofNullable(tarefaDTO);

		return optTarefaDto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
	}
	
	@GetMapping
    @Operation(summary = "Listar tarefas", 
			   description = "Permite consultar a listagem de tarefas",
    		   security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponse(responseCode = "200", description = "Busca realizada com sucesso",
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "401", description = "Não autenticado", 
	    content = @Content(schema = @Schema(defaultValue = "")))
	public ResponseEntity<List<TarefaDTO>> listarTarefas() {
		List<Tarefa> tarefas = this.service.listarTarefas();
		
		List<TarefaDTO> tarefasDTO = tarefas.stream().map(tarefa -> TarefaDTO.create()
						.withId(tarefa.getId())
						.withTitulo(tarefa.getTitulo())
						.withStatus(tarefa.getStatus())
						.withUsuarioId(tarefa.getUsuario().getId())
						.withDescricao(tarefa.getDescricao())
						.withDataCriacao(tarefa.getDataCriacao())
						.withDataAtualizacao(tarefa.getDataAtualizacao())
						.add(linkTo(methodOn(TarefaController.class).buscarPeloId(tarefa.getId())).withSelfRel()))
					.collect(Collectors.toList());

		return ResponseEntity.ok(tarefasDTO);
	}
	
	@GetMapping("/paginado")
    @Operation(summary = "Listar tarefas com filtro", 
			   description = "Permite consultar tarefas com ou sem filtros",
    		   security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponse(responseCode = "200", description = "Busca realizada com sucesso",
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "401", description = "Não autenticado", 
	    content = @Content(schema = @Schema(defaultValue = "")))
	public ResponseEntity<Page<TarefaDTO>> listarTarefasPaginadasComFiltro(
			@RequestParam(required = false) @Parameter(description = "Id (Igual)") UUID id,
			@RequestParam(required = false) @Parameter(description = "Usuário Id (Igual)") UUID usuarioId,
			@RequestParam(required = false) @Parameter(description = "Título (Contendo %)") String titulo,
			@RequestParam(required = false) @Parameter(description = "Descrição (% Contendo %)") String descricao,
			@RequestParam(required = false) @Parameter(description = "Status (Igual)") StatusTarefa status,
			@RequestParam(required = false) 
				@Parameter(description = "Formato: yyyy-MM-dd'T'HH:mm:ss entre dataAtualizacaoInicio e dataAtualizacaoFim)") 
				@DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime dataAtualizacaoInicio,
			@RequestParam(required = false) 
				@Parameter(description = "Formato: yyyy-MM-dd'T'HH:mm:ss entre dataAtualizacaoInicio e dataAtualizacaoFim)") 
				@DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime dataAtualizacaoFim,
			@RequestParam(required = false) 
				@Parameter(description = "Formato: yyyy-MM-dd'T'HH:mm:ss entre dataCriacaoInicio e dataCriacaoFim)")
				@DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime dataCriacaoInicio,
			@RequestParam(required = false) 
				@Parameter(description = "Formato: yyyy-MM-dd'T'HH:mm:ss entre dataCriacaoInicio e dataCriacaoFim)")
				@DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime dataCriacaoFim,
			@RequestParam(name = "Size", defaultValue = "10") Integer size,
			@RequestParam(name = "Page", defaultValue = "1") Integer page) {
		
		Pageable pageable = PageRequest.of(page, size);

		FiltroTarefaDTO filtro = FiltroTarefaDTO.builder()
				.id(id)
				.usuarioId(usuarioId)
				.titulo(titulo)
				.descricao(descricao)
				.status(status)
				.dataCriacaoInicio(dataCriacaoInicio)
				.dataCriacaoFim(dataCriacaoFim)
				.dataAtualizacaoInicio(dataAtualizacaoInicio)
				.dataAtualizacaoFim(dataAtualizacaoFim)
				.build();
		
		List<Tarefa> tarefas = this.service.listarTarefasComFiltro(filtro);
		
		List<TarefaDTO> tarefasDTO = tarefas.stream().map(tarefa -> TarefaDTO.create()
						.withId(tarefa.getId())
						.withTitulo(tarefa.getTitulo())
						.withDescricao(tarefa.getDescricao())
						.withStatus(tarefa.getStatus())
						.withUsuarioId(tarefa.getUsuario().getId())
						.withDataCriacao(tarefa.getDataCriacao())
						.withDataAtualizacao(tarefa.getDataAtualizacao())
						.add(linkTo(methodOn(TarefaController.class).buscarPeloId(tarefa.getId())).withSelfRel()))
					.collect(Collectors.toList());

		return ResponseEntity.ok(new PageImpl<>(tarefasDTO, pageable, tarefasDTO.stream().count()));
	}
}
