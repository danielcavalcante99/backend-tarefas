package br.com.tarefa.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.tarefa.entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID>{
	
	Optional<Usuario> findByNomeUsuario(String nomeUsuario);

}