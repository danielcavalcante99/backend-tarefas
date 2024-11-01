package br.com.tarefa.services.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.tarefa.entities.Usuario;
import br.com.tarefa.services.UsuarioService;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioService service;
    
	public CustomUserDetailsService(UsuarioService service) {
		this.service = service;
	}

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = this.service.buscarPeloNomeUsuario(username); 
        
        if(usuario == null) {
            throw new UsernameNotFoundException(String.format("Usuário %s desconhecido", username));
        }
        
        return User.withUsername(usuario.getNomeUsuario())
                .password(usuario.getSenha())
                .authorities("ROLE_USER")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}