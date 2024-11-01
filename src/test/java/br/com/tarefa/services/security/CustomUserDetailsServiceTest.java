package br.com.tarefa.services.security;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

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
				.nome("daniel")
				.nomeUsuario("danielsilva")
				.senha(new BCryptPasswordEncoder().encode("12345678"))
				.dataAtualizacao(LocalDateTime.now())
				.dataCriacao(LocalDateTime.now())
				.build();

		when(this.usuarioService.buscarPeloNomeUsuario(usuario.getNomeUsuario())).thenReturn(usuario);

		UserDetails userDetails = this.service.loadUserByUsername(usuario.getNomeUsuario());

		assertAll(() -> assertEquals(userDetails.getUsername(), usuario.getNomeUsuario()),
				() -> assertEquals(userDetails.getPassword(), usuario.getSenha()));

	}

	@Test
	void testeTentarCarregarUsuarioPeloNomeUsuario() {
		String nomeUsuario = "danielsilva";

		when(this.usuarioService.buscarPeloNomeUsuario(nomeUsuario)).thenReturn(null);

		try {
			this.service.loadUserByUsername(nomeUsuario);

		} catch (UsernameNotFoundException e) {
			assertEquals(e.getMessage(), String.format("Usuário %s desconhecido", nomeUsuario));
		}
	}

}
