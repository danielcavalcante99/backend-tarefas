package br.com.tarefa.services.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.tarefa.entities.Usuario;
import br.com.tarefa.services.UsuarioService;

class CustomUserDetailsServiceTest {

	@Mock
	private UsuarioService usuarioService;

	private CustomUserDetailsService service;

	@BeforeEach
	void initMocks() {
		MockitoAnnotations.openMocks(this);
		this.service = new CustomUserDetailsService(usuarioService);
	}

	@Test
	void testeCarregarUsuarioPeloNomeUsuarioComSucesso() {
		Usuario usuario = Usuario.builder()
				.id(UUID.randomUUID())
				.nome(RandomStringUtils.randomAlphabetic(10))
				.nomeUsuario(RandomStringUtils.randomAlphabetic(10))
				.senha(new BCryptPasswordEncoder().encode(RandomStringUtils.randomAlphabetic(10)))
				.dataAtualizacao(LocalDateTime.now())
				.dataCriacao(LocalDateTime.now())
				.build();
		
		// Mockando o comportamento do serviço na busca pelo nome de usuário
		when(this.usuarioService.buscarPeloNomeUsuario(usuario.getNomeUsuario())).thenReturn(usuario);

		UserDetails userDetails = this.service.loadUserByUsername(usuario.getNomeUsuario());

		assertEquals(usuario.getNomeUsuario(), userDetails.getUsername());
		assertEquals(usuario.getSenha(), userDetails.getPassword());

	}

	@Test
	void testeTentarCarregarUsuarioPeloNomeUsuarioQueNaoExiste() {
		String nomeUsuario = RandomStringUtils.randomAlphabetic(10);

		// Mockando o comportamento do serviço na busca pelo nome de usuário
		when(this.usuarioService.buscarPeloNomeUsuario(nomeUsuario)).thenReturn(null);
		
		UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
			this.service.loadUserByUsername(nomeUsuario);
        });

		assertEquals(String.format(
				"O token informado está associado ao usuário '%s', que não está mais registrado na base de dados",
				nomeUsuario), exception.getMessage());
	}

}
