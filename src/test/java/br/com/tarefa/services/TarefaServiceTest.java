package br.com.tarefa.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import br.com.tarefa.dtos.AtualizarTarefaDTO;
import br.com.tarefa.dtos.CriarTarefaDTO;
import br.com.tarefa.entities.Tarefa;
import br.com.tarefa.entities.Usuario;
import br.com.tarefa.entities.enums.StatusTarefa;
import br.com.tarefa.exceptions.AuthorizationException;
import br.com.tarefa.exceptions.BusinessException;
import br.com.tarefa.exceptions.ResourceNotFoundException;
import br.com.tarefa.mappers.TarefaMapper;
import br.com.tarefa.mappers.TarefaMapperImpl;
import br.com.tarefa.repositories.TarefaRepository;
import br.com.tarefa.utils.UsuarioUtils;

class TarefaServiceTest {
	
	private TarefaService service;
	private TarefaMapper mapper;
	private String usuarioLogado;
	private LocalDateTime dataAtual;
	private MockedStatic<UsuarioUtils> mockedStaticUsuarioUtils;
	
	@Mock private TarefaRepository repository;
	@Mock private UsuarioService usuarioService;

	@BeforeEach
	void initMocks() {
		MockitoAnnotations.openMocks(this);
		this.mockedStaticUsuarioUtils = mockStatic(UsuarioUtils.class);
		this.mapper = new TarefaMapperImpl();
		this.service = new TarefaService(repository, usuarioService, mapper);
		this.usuarioLogado = "daniel";
		this.dataAtual = LocalDateTime.now();
	}
	
    @AfterEach
    public void cleanup() {
    	this.mockedStaticUsuarioUtils.close();
    }
    
    private CriarTarefaDTO instanceCriarTarefaDTO() {
    	return CriarTarefaDTO.builder()
				.titulo("Tarefa A")
				.descricao("Descrição tarefa A").build();
    }
    
    private AtualizarTarefaDTO instanceAtualizarTarefaDTO() {
    	return AtualizarTarefaDTO.builder()
				.id(UUID.randomUUID())
				.titulo("Tarefa B")
				.descricao("Descrição tarefa B")
				.status(StatusTarefa.ANDAMENTO)
				.build();
    }
    
    private Tarefa instanceEntityTarefa() {
    	return Tarefa.builder()
				.id(this.instanceAtualizarTarefaDTO().getId())
				.titulo("Tarefa A")
				.descricao("Descrição tarefa A")
				.status(StatusTarefa.PENDENTE)
				.dataCriacao(this.dataAtual)
				.dataAtualizacao(this.dataAtual)
				.usuario(Usuario.builder().nomeUsuario(this.usuarioLogado).build())
				.build();
    }
	
	@Test
	void testeCriarTarefaComSucesso() {
		CriarTarefaDTO criarTarefaDTO = this.instanceCriarTarefaDTO();	
		
		when(this.repository.findByTitulo(criarTarefaDTO.getTitulo())).thenReturn(Optional.ofNullable(null));	
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(this.usuarioLogado);
		when(this.usuarioService.buscarPeloNomeUsuario(this.usuarioLogado)).thenReturn(new Usuario()); 
		
		Tarefa tarefa = this.service.criarTarefa(criarTarefaDTO);
		assertAll(() -> assertEquals(tarefa.getTitulo(), criarTarefaDTO.getTitulo()),
				  () -> assertEquals(tarefa.getDescricao(), criarTarefaDTO.getDescricao()),
				  () -> assertEquals(tarefa.getStatus(), StatusTarefa.PENDENTE),
				  () -> assertEquals(tarefa.getDataCriacao(), tarefa.getDataAtualizacao()),
				  () -> verify(this.usuarioService).buscarPeloNomeUsuario(any()),
				  () -> verify(this.repository).save(any()));
		
	}
	
	@Test
	void testeTentarCriarTarefaComTituloJaCadastrado() {
		CriarTarefaDTO criarTarefaDTO = this.instanceCriarTarefaDTO();
		
		when(this.repository.findByTitulo(criarTarefaDTO.getTitulo()))
				.thenReturn(Optional.of(new Tarefa()));	
		try {
			this.service.criarTarefa(criarTarefaDTO);
			
		} catch (BusinessException e) {
			assertAll(() -> assertEquals(e.getMessage(), String.format("Título %s já está cadastrado em outra tarefa", criarTarefaDTO.getTitulo())),
					  () -> verify(this.usuarioService, never()).buscarPeloNomeUsuario(any()),
					  () -> verify(this.repository, never()).save(any()));
		}

	}
	
	@Test
	void testeAtualizarTarefaComSucesso() {	
		AtualizarTarefaDTO atualizarTarefaDTO = this.instanceAtualizarTarefaDTO();
		Tarefa tarefaEntity = instanceEntityTarefa();
		
		when(this.repository.findById(atualizarTarefaDTO.getId())).thenReturn(Optional.of(tarefaEntity));	
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(this.usuarioLogado);
		
		Tarefa tarefa = this.service.atualizarTarefa(atualizarTarefaDTO);
		
		assertAll(() -> assertEquals(tarefa.getTitulo(), atualizarTarefaDTO.getTitulo()),
				  () -> assertEquals(tarefa.getDescricao(), atualizarTarefaDTO.getDescricao()),
				  () -> assertEquals(tarefa.getStatus(), atualizarTarefaDTO.getStatus()),
				  () -> verify(this.repository).findById(any()),
				  () -> verify(this.repository).save(any()));
		
	}
	
	@Test
	void testeTentarAtualizarTarefaNaoExiste() {	
		AtualizarTarefaDTO atualizarTarefaDTO = this.instanceAtualizarTarefaDTO();
		
		when(this.repository.findById(atualizarTarefaDTO.getId())).thenReturn(Optional.ofNullable(null));	
		
		try {
			 this.service.atualizarTarefa(atualizarTarefaDTO);
			
		} catch (ResourceNotFoundException e) {
			assertAll(() -> assertEquals(e.getMessage(), String.format("Tarefa pelo id %s não existe", atualizarTarefaDTO.getId())),
					  () -> verify(this.repository, never()).save(any()));
		}
	}
	
	@Test
	void testeTentarAtualizarTarefaDeOutroUsuario() {	
		String outroUsuario = "lucas";
		AtualizarTarefaDTO atualizarTarefaDTO = this.instanceAtualizarTarefaDTO();
		Tarefa tarefaEntity = this.instanceEntityTarefa();
		
		when(this.repository.findById(atualizarTarefaDTO.getId())).thenReturn(Optional.of(tarefaEntity));	
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(outroUsuario);
		
		try {
			 this.service.atualizarTarefa(atualizarTarefaDTO);
			
		} catch (AuthorizationException e) {
			assertAll(() -> assertEquals(e.getMessage(), "Apenas o usuário que criou a tarefa pode atualizá-la"),
					  () -> verify(this.repository, never()).save(any()));
		}
	}
	
	@Test
	void testeExcluirTarefaPeloIdComSucesso() {	
		Tarefa tarefaEntity = this.instanceEntityTarefa();
		
		when(this.repository.findById(tarefaEntity.getId())).thenReturn(Optional.of(tarefaEntity));	
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(this.usuarioLogado);
		
		this.service.excluirTarefaPeloId(tarefaEntity.getId());
		
		assertAll(() -> verify(this.repository).findById(any()),
				  () -> verify(this.repository).deleteById(any()));
	}
	
	@Test
	void testeTentarExcluirTarefaNaoExiste() {
		UUID id = UUID.randomUUID();
		when(this.repository.findById(id)).thenReturn(Optional.ofNullable(null));

		try {
			this.service.excluirTarefaPeloId(id);

		} catch (ResourceNotFoundException e) {
			assertAll(
					() -> assertEquals(e.getMessage(), String.format("Tarefa pelo id %s não existe", id)),
					() -> verify(this.repository, never()).deleteById(any()));
		}
	}
	
	@Test
	void testeTentarExcluirTarefaDeOutroUsuario() {
		String outroUsuario = "daniel";
		Tarefa tarefaEntity = this.instanceEntityTarefa();
		
		when(this.repository.findById(tarefaEntity.getId())).thenReturn(Optional.of(tarefaEntity));	
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(outroUsuario);
		
		try {
			this.service.excluirTarefaPeloId(tarefaEntity.getId());

		} catch (AuthorizationException e) {
			assertAll(
					() -> assertEquals(e.getMessage(), "Apenas o usuário que criou a tarefa tem permissão para excluí-la"),
					() -> verify(this.repository, never()).deleteById(any()));
		}
	}

}
