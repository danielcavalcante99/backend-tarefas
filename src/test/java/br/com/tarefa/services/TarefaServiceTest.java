package br.com.tarefa.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
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
		this.usuarioLogado = RandomStringUtils.randomAlphabetic(10);
		this.dataAtual = LocalDateTime.now();
	}
	
    @AfterEach
    public void cleanup() {
    	this.mockedStaticUsuarioUtils.close();
    }
	
	@Test
	void testeCriarTarefaComSucesso() {
		CriarTarefaDTO criarTarefaDTO = this.createInstanceCriarTarefaDTO();	
		
		// Mockando o comportamento do repositorio na busca pelo título da tarefa
		when(this.repository.findByTitulo(criarTarefaDTO.getTitulo())).thenReturn(Optional.ofNullable(null));	
		// Mockando o nome do usuário logado
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(this.usuarioLogado);
		// Mockando o comportamento da busca pelo nome do usuário
		when(this.usuarioService.buscarPeloNomeUsuario(this.usuarioLogado)).thenReturn(new Usuario()); 
		
		Tarefa tarefa = this.service.criarTarefa(criarTarefaDTO);
		
		assertEquals(tarefa.getTitulo(), criarTarefaDTO.getTitulo());
		assertEquals(tarefa.getDescricao(), criarTarefaDTO.getDescricao());
		assertEquals(tarefa.getStatus(), StatusTarefa.PENDENTE);
		assertEquals(tarefa.getDataCriacao(), tarefa.getDataAtualizacao());
		verify(this.usuarioService).buscarPeloNomeUsuario(any());
		verify(this.repository).save(any());
	}
	
	@Test
	void testeTentarCriarTarefaComTituloJaCadastrado() {
		CriarTarefaDTO criarTarefaDTO = this.createInstanceCriarTarefaDTO();
		
		// Mockando o comportamento do repositorio na busca pelo título da tarefa
		when(this.repository.findByTitulo(criarTarefaDTO.getTitulo())).thenReturn(Optional.of(new Tarefa()));	
		
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			this.service.criarTarefa(criarTarefaDTO);
        });
		
		assertEquals(String.format("Título %s já está cadastrado em outra tarefa", criarTarefaDTO.getTitulo()), exception.getMessage());
		verify(this.usuarioService, never()).buscarPeloNomeUsuario(any());
		verify(this.repository, never()).save(any());
	}
	
	@Test
	void testeAtualizarTarefaComSucesso() {	
		AtualizarTarefaDTO atualizarTarefaDTO = this.createInstanceAtualizarTarefaDTO();
		Tarefa tarefaEntity = createInstanceEntityTarefa();
		
		// Mockando o comportamento do repositorio na busca pelo id da tarefa
		when(this.repository.findById(atualizarTarefaDTO.getId())).thenReturn(Optional.of(tarefaEntity));
		// Mockando o nome do usuário logado
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(this.usuarioLogado);
		
		Tarefa tarefa = this.service.atualizarTarefa(atualizarTarefaDTO);
		
		assertEquals(atualizarTarefaDTO.getTitulo(), tarefa.getTitulo());
		assertEquals(atualizarTarefaDTO.getDescricao(), tarefa.getDescricao());
		assertEquals(atualizarTarefaDTO.getStatus(), tarefa.getStatus());
		verify(this.repository).findById(any());
		verify(this.repository).save(any());
	}
	
	@Test
	void testeTentarAtualizarTarefaNaoExiste() {	
		AtualizarTarefaDTO atualizarTarefaDTO = this.createInstanceAtualizarTarefaDTO();
		
		// Mockando o comportamento do repositorio na busca pelo id da tarefa
		when(this.repository.findById(atualizarTarefaDTO.getId())).thenReturn(Optional.ofNullable(null));	
		
		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
			 this.service.atualizarTarefa(atualizarTarefaDTO);
        });
		
		assertEquals(String.format("Tarefa pelo id %s não existe", atualizarTarefaDTO.getId()), exception.getMessage());
		verify(this.repository, never()).save(any());
	}
	
	@Test
	void testeTentarAtualizarTarefaDeOutroUsuario() {	
		String outroUsuario = RandomStringUtils.randomAlphabetic(11);
		AtualizarTarefaDTO atualizarTarefaDTO = this.createInstanceAtualizarTarefaDTO();
		Tarefa tarefaEntity = this.createInstanceEntityTarefa();
		
		// Mockando o comportamento do repositorio na busca pelo id da tarefa
		when(this.repository.findById(atualizarTarefaDTO.getId())).thenReturn(Optional.of(tarefaEntity));	
		// Mockando o nome do usuário logado
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(outroUsuario);
		
		AuthorizationException exception = assertThrows(AuthorizationException.class, () -> {
			 this.service.atualizarTarefa(atualizarTarefaDTO);
        });
		
		assertEquals("Apenas o usuário que criou a tarefa pode atualizá-la", exception.getMessage());
		verify(this.repository, never()).save(any());
	}
	
	@Test
	void testeAtualizarTarefaComTituloCadastroPeloOutroUsuario() {	
		AtualizarTarefaDTO atualizarTarefaDTO = this.createInstanceAtualizarTarefaDTO();
		Tarefa tarefaEntity = createInstanceEntityTarefa();
		
		// Mockando o comportamento do repositorio na busca pelo id da tarefa
		when(this.repository.findById(atualizarTarefaDTO.getId())).thenReturn(Optional.of(tarefaEntity));	
		// Mockando o nome do usuário logado
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(this.usuarioLogado);
		// Mockando o comportamento do repositorio na busca pelo título da tarefa
		when(this.repository.findByTitulo(atualizarTarefaDTO.getTitulo())).thenReturn(Optional.of(Tarefa.builder()
				.id(UUID.randomUUID())
				.titulo(RandomStringUtils.randomAlphabetic(10))
				.descricao(RandomStringUtils.randomAlphabetic(50))
				.status(StatusTarefa.PENDENTE)
				.dataCriacao(this.dataAtual)
				.dataAtualizacao(this.dataAtual)
				.usuario(Usuario.builder().nomeUsuario(this.usuarioLogado).build())
				.build()));	
		
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			 this.service.atualizarTarefa(atualizarTarefaDTO);
        });
		
		verify(this.repository).findById(any());
		verify(this.repository).findByTitulo(any());
		verify(this.repository, never()).save(any());
		assertEquals(String.format("Título %s já está cadastrado em outra tarefa de outro usuário", atualizarTarefaDTO.getTitulo()), 
				exception.getMessage());
	}
	
	@Test
	void testeExcluirTarefaPeloIdComSucesso() {	
		Tarefa tarefaEntity = this.createInstanceEntityTarefa();
		
		// Mockando o comportamento do repositorio na busca pelo id da tarefa
		when(this.repository.findById(tarefaEntity.getId())).thenReturn(Optional.of(tarefaEntity));	
		// Mockando o nome do usuário logado
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(this.usuarioLogado);
		
		this.service.excluirTarefaPeloId(tarefaEntity.getId());
		
		verify(this.repository).findById(any());
		verify(this.repository).deleteById(any());
	}
	
	@Test
	void testeTentarExcluirTarefaNaoExiste() {
		UUID id = UUID.randomUUID();
		
		// Mockando o comportamento do repositorio na busca pelo id da tarefa
		when(this.repository.findById(id)).thenReturn(Optional.ofNullable(null));
		
		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
			this.service.excluirTarefaPeloId(id);
        });

		assertEquals(String.format("Tarefa pelo id %s não existe", id), exception.getMessage());
		verify(this.repository, never()).deleteById(any());
	}
	
	@Test
	void testeTentarExcluirTarefaDeOutroUsuario() {
		String outroUsuario = RandomStringUtils.randomAlphabetic(7);
		Tarefa tarefaEntity = this.createInstanceEntityTarefa();
		
		// Mockando o comportamento do repositorio na busca pelo id da tarefa
		when(this.repository.findById(tarefaEntity.getId())).thenReturn(Optional.of(tarefaEntity));	
		// Mockando o nome do usuário logado
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(outroUsuario);
		
		AuthorizationException exception = assertThrows(AuthorizationException.class, () -> {
			this.service.excluirTarefaPeloId(tarefaEntity.getId());
        });

		assertEquals("Apenas o usuário que criou a tarefa tem permissão para excluí-la", exception.getMessage());
		verify(this.repository, never()).deleteById(any());
	}
	
    private CriarTarefaDTO createInstanceCriarTarefaDTO() {
    	return CriarTarefaDTO.builder()
				.titulo("Tarefa A")
				.descricao("Descrição tarefa A").build();
    }
    
    private AtualizarTarefaDTO createInstanceAtualizarTarefaDTO() {
    	return AtualizarTarefaDTO.builder()
				.id(UUID.randomUUID())
				.titulo(RandomStringUtils.randomAlphabetic(10))
				.descricao(RandomStringUtils.randomAlphabetic(50))
				.status(StatusTarefa.ANDAMENTO)
				.build();
    }
    
    private Tarefa createInstanceEntityTarefa() {
    	return Tarefa.builder()
				.id(this.createInstanceAtualizarTarefaDTO().getId())
				.titulo(RandomStringUtils.randomAlphabetic(10))
				.descricao(RandomStringUtils.randomAlphabetic(50))
				.status(StatusTarefa.PENDENTE)
				.dataCriacao(this.dataAtual)
				.dataAtualizacao(this.dataAtual)
				.usuario(Usuario.builder().nomeUsuario(this.usuarioLogado).build())
				.build();
    }

}
