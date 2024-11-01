package br.com.tarefa.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.tarefa.dtos.CriarTarefaDTO;
import br.com.tarefa.dtos.TarefaDTO;
import br.com.tarefa.entities.Tarefa;

@Mapper(componentModel = "spring")
public interface TarefaMapper {

	@Mapping(target = "usuario", ignore = true)
	Tarefa tarefaDTOToTarefa(TarefaDTO dto);
	
	@Mapping(target = "usuarioId", source = "usuario.id")
	TarefaDTO tarefaToTarefaDTO(Tarefa tarefa);
	
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "usuario", ignore = true)
	@Mapping(target = "dataCriacao", ignore = true)
	@Mapping(target = "dataAtualizacao", ignore = true)
	Tarefa criarTarefaDTOToTarefa(CriarTarefaDTO dto);

}
