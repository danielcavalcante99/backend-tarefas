package br.com.tarefa.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.tarefa.dtos.AtualizarUsuarioDTO;
import br.com.tarefa.dtos.CriarUsuarioDTO;
import br.com.tarefa.entities.Usuario;
import br.com.tarefa.exceptions.AuthorizationException;
import br.com.tarefa.exceptions.BusinessException;
import br.com.tarefa.exceptions.ResourceNotFoundException;
import br.com.tarefa.mappers.UsuarioMapper;
import br.com.tarefa.mappers.UsuarioMapperImpl;
import br.com.tarefa.repositories.UsuarioRepository;
import br.com.tarefa.utils.UsuarioUtils;

class UsuarioServiceTest {
	
	private UsuarioMapper mapper;
	
	private String usuarioLogado;
	private LocalDateTime dataAtual;
	private MockedStatic<UsuarioUtils> mockedStaticUsuarioUtils;
	
	@Mock private UsuarioRepository repository;
     private UsuarioService service;
	
	
	@BeforeEach
	void initMocks() {
		MockitoAnnotations.openMocks(this);
		this.mockedStaticUsuarioUtils = mockStatic(UsuarioUtils.class);
		this.mapper = new UsuarioMapperImpl();
		this.service = new UsuarioService(repository, mapper);
		this.usuarioLogado = "daniel";
		this.dataAtual = LocalDateTime.now();
	}
	
    @AfterEach
    public void cleanup() {
    	this.mockedStaticUsuarioUtils.close();
    }
	
    private CriarUsuarioDTO instanceCriarUsuarioDTO() {
    	return CriarUsuarioDTO.builder()
    			.nome("Daniel")
    			.nomeUsuario(this.usuarioLogado)
				.senha("12345678").build();
    }
    
    private AtualizarUsuarioDTO instanceAtualizarUsuarioDTO() {
    	return AtualizarUsuarioDTO.builder()
    			.nome("Daniel")
    			.nomeUsuario(this.usuarioLogado)
				.senha("12345678")
				.build();
    }
    
    private Usuario instanceEntityUsuario() {
    	return Usuario.builder()
				.id(UUID.randomUUID())
				.nome("Daniel")
				.nomeUsuario(this.usuarioLogado)
				.senha("12345678")
				.dataCriacao(this.dataAtual)
				.dataAtualizacao(this.dataAtual)
				.build();
    }

	@Test
	void testeCriarUsuarioComSucesso() {
		CriarUsuarioDTO criarUsuarioDTO = this.instanceCriarUsuarioDTO();	
		
		when(this.repository.findByNomeUsuario(criarUsuarioDTO.getNomeUsuario()))
				.thenReturn(Optional.ofNullable(null));
		
		Usuario usuario = this.service.criarUsuario(criarUsuarioDTO);
		
		assertAll(() -> assertEquals(usuario.getNome(), criarUsuarioDTO.getNome()),
				  () -> assertEquals(usuario.getNomeUsuario(), criarUsuarioDTO.getNomeUsuario()),
				  () -> assertTrue(new BCryptPasswordEncoder().matches(criarUsuarioDTO.getSenha(), usuario.getSenha())),
				  () -> assertEquals(usuario.getDataCriacao(), usuario.getDataAtualizacao()),
				  () -> verify(this.repository).findByNomeUsuario(any()),
				  () -> verify(this.repository).save(any()));
	}
	
	@Test
	void testeTentarCriarUsuarioQueJaExiste() {
		CriarUsuarioDTO criarUsuarioDTO = this.instanceCriarUsuarioDTO();	
		
		when(this.repository.findByNomeUsuario(criarUsuarioDTO.getNomeUsuario()))
				.thenReturn(Optional.of(this.instanceEntityUsuario()));
		try {
			 this.service.criarUsuario(criarUsuarioDTO);
			
		} catch (BusinessException e) {
			assertAll(() -> assertEquals(e.getMessage(), String.format("Nome de usuário %s já existente", criarUsuarioDTO.getNomeUsuario())),
					  () -> verify(this.repository, never()).save(any()));
		}
	}

	@Test
	void testeAtualizarUsuarioComSucesso() {
		AtualizarUsuarioDTO atualizarUsuarioDTO = this.instanceAtualizarUsuarioDTO();	
		
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(this.usuarioLogado);
		when(this.repository.findByNomeUsuario(atualizarUsuarioDTO.getNomeUsuario()))
				.thenReturn(Optional.of(this.instanceEntityUsuario()));
		
		Usuario usuario = this.service.atualizarUsuario(atualizarUsuarioDTO);
		
		assertAll(() -> assertEquals(usuario.getNome(), atualizarUsuarioDTO.getNome()),
				  () -> assertEquals(usuario.getNomeUsuario(), atualizarUsuarioDTO.getNomeUsuario()),
				  () -> assertTrue(new BCryptPasswordEncoder().matches(atualizarUsuarioDTO.getSenha(), usuario.getSenha())),				  
				  () -> verify(this.repository).findByNomeUsuario(any()),
				  () -> verify(this.repository).save(any()));
	}
	
	@Test
	void testeTentarAtualizarOutroUsuario() {
		AtualizarUsuarioDTO atualizarUsuarioDTO = this.instanceAtualizarUsuarioDTO();	

		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(this.usuarioLogado);
		when(this.repository.findByNomeUsuario(this.usuarioLogado))
			.thenReturn(Optional.of(this.instanceEntityUsuario()));
		
		String outroUsuario = "dansilva";
		when(this.repository.findByNomeUsuario(outroUsuario))
		.thenReturn(Optional.of(new Usuario()));
		
		try {
			this.service.atualizarUsuario(atualizarUsuarioDTO);
			
		} catch (AuthorizationException e) {
			assertAll(() -> assertEquals(e.getMessage(), String.format("O nome de usuário %s já está associado a outro usuário", atualizarUsuarioDTO.getNomeUsuario())),
					  () -> verify(this.repository).findByNomeUsuario(any()),  
					  () -> verify(this.repository, never()).save(any()));
		}
	}

	@Test
	void testeExcluirUsuarioPeloIdComSucesso() {
		Usuario usuarioEntity = this.instanceEntityUsuario();
		
		when(this.repository.findById(usuarioEntity.getId())).thenReturn(Optional.of(usuarioEntity));	
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(this.usuarioLogado);
		
		this.service.excluirTarefaPeloId(usuarioEntity.getId());
		
		assertAll(() -> verify(this.repository).findById(any()),
				  () -> verify(this.repository).deleteById(any()));
	}
	
	@Test
	void testeTentarExcluirUsuarioNaoExiste() {
		UUID id = UUID.randomUUID();
		when(this.repository.findById(id)).thenReturn(Optional.ofNullable(null));

		try {
			this.service.excluirTarefaPeloId(id);

		} catch (ResourceNotFoundException e) {
			assertAll(
					() -> assertEquals(e.getMessage(), String.format("Usuário pelo id %s não existe", id)),
					() -> verify(this.repository, never()).deleteById(any()));
		}
	}
	
	@Test
	void testeTentarExcluirOutroUsuario() {
		String outroUsuario = "dansilva";
		Usuario usuarioEntity = this.instanceEntityUsuario();
		
		when(this.repository.findById(usuarioEntity.getId())).thenReturn(Optional.of(usuarioEntity));	
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(outroUsuario);
		
		try {
			this.service.excluirTarefaPeloId(usuarioEntity.getId());

		} catch (AuthorizationException e) {
			assertAll(
					() -> assertEquals(e.getMessage(), "Apenas o próprio usuário tem permissão para se excluir"),
					() -> verify(this.repository, never()).deleteById(any()));
		}
	}

}
