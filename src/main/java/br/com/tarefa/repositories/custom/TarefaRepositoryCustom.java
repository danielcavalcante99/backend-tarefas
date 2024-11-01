package br.com.tarefa.repositories.custom;

import java.util.List;

import br.com.tarefa.dtos.FiltroTarefaDTO;
import br.com.tarefa.entities.Tarefa;

public interface TarefaRepositoryCustom {

	List<Tarefa> findAllByFilter(FiltroTarefaDTO filtro);

}
