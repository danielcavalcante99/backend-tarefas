package br.com.tarefa.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.tarefa.entities.Tarefa;
import br.com.tarefa.repositories.custom.TarefaRepositoryCustom;


public interface TarefaRepository extends JpaRepository<Tarefa, UUID>, TarefaRepositoryCustom {
	
	Optional<Tarefa> findByTitulo(String titulo);

}
