package br.com.tarefa.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
	@Mock private CacheManager cacheManager;
    @Mock private Cache cache;
    private UsuarioService service;
	
	@BeforeEach
	void initMocks() {
		MockitoAnnotations.openMocks(this);
		this.mockedStaticUsuarioUtils = mockStatic(UsuarioUtils.class);
		this.mapper = new UsuarioMapperImpl();
		this.service = new UsuarioService(repository, mapper, cacheManager);
		this.usuarioLogado = RandomStringUtils.randomAlphabetic(10);
		this.dataAtual = LocalDateTime.now();
		when(cacheManager.getCache("usuarios")).thenReturn(cache);
	}
	
    @AfterEach
    public void cleanup() {
    	this.mockedStaticUsuarioUtils.close();
    }

	@Test
	void testeCriarUsuarioComSucesso() {
		CriarUsuarioDTO criarUsuarioDTO = this.createInstanceCriarUsuarioDTO();	
		
		// Mockando o comportamento do repositorio na busca pelo nome de usuário
		when(this.repository.findByNomeUsuario(criarUsuarioDTO.getNomeUsuario())).thenReturn(Optional.ofNullable(null));
		
		Usuario usuario = this.service.criarUsuario(criarUsuarioDTO);
		
		assertEquals(criarUsuarioDTO.getNome(), usuario.getNome());
		assertEquals(criarUsuarioDTO.getNomeUsuario(), usuario.getNomeUsuario());
		assertTrue(new BCryptPasswordEncoder().matches(criarUsuarioDTO.getSenha(), usuario.getSenha()));
		assertEquals(usuario.getDataCriacao(), usuario.getDataAtualizacao());
		verify(this.repository).findByNomeUsuario(any());
		verify(this.repository).save(any());
	}
	
	@Test
	void testeTentarCriarUsuarioQueJaExiste() {
		CriarUsuarioDTO criarUsuarioDTO = this.createInstanceCriarUsuarioDTO();	
		
		// Mockando o comportamento do repositorio na busca pelo nome de usuário
		when(this.repository.findByNomeUsuario(criarUsuarioDTO.getNomeUsuario())).thenReturn(Optional.of(this.createInstanceEntityUsuario()));
		
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			this.service.criarUsuario(criarUsuarioDTO);
        });

		assertEquals(String.format("Nome de usuário %s já existente", criarUsuarioDTO.getNomeUsuario()), exception.getMessage());
		verify(this.repository, never()).save(any());
	
	}

	@Test
	void testeAtualizarUsuarioComSucesso() {
		AtualizarUsuarioDTO atualizarUsuarioDTO = this.createInstanceAtualizarUsuarioDTO();	
		
		// Mockando o nome do usuário logado
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(this.usuarioLogado);
		
		// Mockando o comportamento do repositorio na busca pelo nome de usuário
		when(this.repository.findByNomeUsuario(atualizarUsuarioDTO.getNomeUsuario())).thenReturn(Optional.of(this.createInstanceEntityUsuario()));
		
		Usuario usuario = this.service.atualizarUsuario(atualizarUsuarioDTO);
		
		assertEquals(atualizarUsuarioDTO.getNome(), usuario.getNome());
		assertEquals(atualizarUsuarioDTO.getNomeUsuario(), usuario.getNomeUsuario());
		assertTrue(new BCryptPasswordEncoder().matches(atualizarUsuarioDTO.getSenha(), usuario.getSenha()));				  
		verify(this.repository).findByNomeUsuario(any());
		verify(this.repository).save(any());
		
		// Capturando o usuário que foi colocado no cache
	    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
	    verify(cache, times(2)).put(captor.capture(), eq(usuario));
	    
        // Verificando se a key nome de usuário foi corretamente colocado no cache
        assertEquals(usuario.getNomeUsuario(), captor.getValue());
	}
	
	@Test
	void testeTentarAtualizarOutroUsuario() {
		String outroUsuario = RandomStringUtils.randomAlphabetic(10);
		AtualizarUsuarioDTO atualizarUsuarioDTO = this.createInstanceAtualizarUsuarioDTO();	
		atualizarUsuarioDTO.setNomeUsuario(outroUsuario);
		
		// Mockando o nome do usuário logado
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(this.usuarioLogado);
		
		// Mockando o comportamento do repositorio na busca pelo nome de usuário
		when(this.repository.findByNomeUsuario(this.usuarioLogado)).thenReturn(Optional.of(this.createInstanceEntityUsuario()));
		
		// Mockando o comportamento do repositorio na busca pelo nome de do outro usuário
		when(this.repository.findByNomeUsuario(outroUsuario))
				.thenReturn(Optional.of(Usuario.builder().id(UUID.randomUUID()).nomeUsuario(outroUsuario).build()));
		
		AuthorizationException exception = assertThrows(AuthorizationException.class, () -> {
			this.service.atualizarUsuario(atualizarUsuarioDTO);
        });
        
		assertEquals(String.format("O nome de usuário %s já está associado a outro usuário", atualizarUsuarioDTO.getNomeUsuario()), exception.getMessage());
		verify(this.repository, times(2)).findByNomeUsuario(any());
		verify(this.repository, never()).save(any());
		verify(cache, never()).put(any(), any());
	}

	@Test
	void testeExcluirUsuarioPeloIdComSucesso() {
		Usuario usuarioEntity = this.createInstanceEntityUsuario();
		
		// Mockando o comportamento do repositorio na busca pelo id de usuário
		when(this.repository.findById(usuarioEntity.getId())).thenReturn(Optional.of(usuarioEntity));
		// Mockando o nome do usuário logado
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(this.usuarioLogado);
		
		this.service.excluirUsuarioPeloId(usuarioEntity.getId());
		
		verify(this.repository).findById(any());
		verify(this.repository).deleteById(any());
	    verify(cache, times(2)).evict(any());
	}
	
	@Test
	void testeTentarExcluirUsuarioNaoExiste() {
		UUID id = UUID.randomUUID();
		
		// Mockando o comportamento do repositorio na busca pelo id de usuário
		when(this.repository.findById(id)).thenReturn(Optional.ofNullable(null));
		
		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
			this.service.excluirUsuarioPeloId(id);
        });
		
		assertEquals(String.format("Usuário pelo id %s não existe", id), exception.getMessage());
		verify(this.repository, never()).deleteById(any());
		verify(cache, never()).evict(any());
	}
	
	@Test
	void testeTentarExcluirOutroUsuario() {
		String outroUsuario = RandomStringUtils.randomAlphabetic(10);
		Usuario usuarioEntity = this.createInstanceEntityUsuario();
		
		// Mockando o comportamento do repositorio na busca pelo id de usuário
		when(this.repository.findById(usuarioEntity.getId())).thenReturn(Optional.of(usuarioEntity));	
		// Mockando o nome do usuário logado
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(outroUsuario);
		
		AuthorizationException exception = assertThrows(AuthorizationException.class, () -> {
			this.service.excluirUsuarioPeloId(usuarioEntity.getId());
        });
		
		assertEquals("Apenas o próprio usuário tem permissão para se excluir", exception.getMessage());
		verify(this.repository, never()).deleteById(any());
		verify(cache, never()).evict(any());
	}
	
	@Test
	void testeTentarConsultarDadosDeOutroUsuario() {
		String outroUsuario = RandomStringUtils.randomAlphabetic(10);
		Usuario usuarioEntity = this.createInstanceEntityUsuario();
		
		// Mockando o comportamento do repositorio na busca pelo id de usuário
		when(this.repository.findById(usuarioEntity.getId())).thenReturn(Optional.of(usuarioEntity));
		// Mockando o nome do usuário logado
		mockedStaticUsuarioUtils.when(UsuarioUtils::getUsuarioLogado).thenReturn(outroUsuario);
		
		AuthorizationException exception = assertThrows(AuthorizationException.class, () -> {
			this.service.buscarPeloId(usuarioEntity.getId());
        });
	
		assertEquals("Apenas o usuário tem permissão para visualizar seus dados", exception.getMessage());
		verify(this.repository, never()).deleteById(any());
	}
	
    private CriarUsuarioDTO createInstanceCriarUsuarioDTO() {
    	return CriarUsuarioDTO.builder()
    			.nome(RandomStringUtils.randomAlphabetic(10))
    			.nomeUsuario(this.usuarioLogado)
				.senha(RandomStringUtils.randomAlphabetic(10)).build();
    }
    
    private AtualizarUsuarioDTO createInstanceAtualizarUsuarioDTO() {
    	return AtualizarUsuarioDTO.builder()
    			.nome(RandomStringUtils.randomAlphabetic(10))
    			.nomeUsuario(this.usuarioLogado)
				.senha(RandomStringUtils.randomAlphabetic(10))
				.build();
    }
    
    private Usuario createInstanceEntityUsuario() {
    	return Usuario.builder()
				.id(UUID.randomUUID())
				.nome(RandomStringUtils.randomAlphabetic(10))
				.nomeUsuario(this.usuarioLogado)
				.senha(RandomStringUtils.randomAlphabetic(10))
				.dataCriacao(this.dataAtual)
				.dataAtualizacao(this.dataAtual)
				.build();
    }

}
