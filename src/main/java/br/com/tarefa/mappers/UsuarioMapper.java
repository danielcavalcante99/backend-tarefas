package br.com.tarefa.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.tarefa.dtos.CriarUsuarioDTO;
import br.com.tarefa.dtos.UsuarioDTO;
import br.com.tarefa.entities.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
	
	@Mapping(target = "tarefas", ignore = true)
	Usuario usuarioDTOToUsuario(UsuarioDTO dto);
	
	UsuarioDTO usuarioToUsuarioDTO(Usuario entity);
	
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "dataCriacao", ignore = true)
	@Mapping(target = "dataAtualizacao", ignore = true)
	Usuario criarUsuarioDTOToUsuario(CriarUsuarioDTO dto);

}
