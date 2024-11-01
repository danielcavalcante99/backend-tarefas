package br.com.tarefa.services;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import br.com.tarefa.dtos.AtualizarUsuarioDTO;
import br.com.tarefa.dtos.CriarUsuarioDTO;
import br.com.tarefa.entities.Usuario;
import br.com.tarefa.exceptions.AuthorizationException;
import br.com.tarefa.exceptions.BusinessException;
import br.com.tarefa.exceptions.ResourceNotFoundException;
import br.com.tarefa.mappers.UsuarioMapper;
import br.com.tarefa.repositories.UsuarioRepository;
import br.com.tarefa.utils.UsuarioUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Validated
public class UsuarioService {
	
	private final UsuarioRepository repository;
	private final UsuarioMapper mapper;

	public UsuarioService(UsuarioRepository repository, UsuarioMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	public Usuario buscarPeloId(@NotNull @Valid UUID id) {
		return this.repository.findById(id).orElse(null);
	}
	
	public Usuario buscarPeloNomeUsuario(@NotNull @Valid String nomeUsuario) {
		return this.repository.findByNomeUsuario(nomeUsuario).orElse(null);
	}
	
	public Usuario criarUsuario(@NotNull @Valid CriarUsuarioDTO dto) throws BusinessException {
		Usuario outroUsuario = this.buscarPeloNomeUsuario(dto.getNomeUsuario());
		
		if(outroUsuario != null)
			throw new BusinessException("Nome de usuário %s já existente", dto.getNomeUsuario());
		
		LocalDateTime dataAtual = LocalDateTime.now();
		Usuario entity = this.mapper.criarUsuarioDTOToUsuario(dto);
		entity.setSenha(new BCryptPasswordEncoder().encode(entity.getSenha()));
		entity.setDataCriacao(dataAtual);
		entity.setDataAtualizacao(dataAtual);
		
		this.repository.save(entity);
		log.info("Usuário {} criado", entity.getNomeUsuario());
		
		return entity;
	}
	
	public Usuario atualizarUsuario(@NotNull @Valid AtualizarUsuarioDTO dto) throws AuthorizationException {
		Usuario entity = this.buscarPeloNomeUsuario(UsuarioUtils.getUsuarioLogado());
		
		if(!entity.getNomeUsuario().equals(dto.getNomeUsuario())) {
			Usuario outroUsuario = this.buscarPeloNomeUsuario(dto.getNomeUsuario());
			
			if(outroUsuario != null && !outroUsuario.getId().equals(entity.getId()))
				throw new AuthorizationException("O nome de usuário %s já está associado a outro usuário", dto.getNomeUsuario());
		}
		
		entity.setNome(dto.getNome());
		entity.setNomeUsuario(dto.getNomeUsuario());
		entity.setSenha(new BCryptPasswordEncoder().encode(dto.getSenha()));
		entity.setDataAtualizacao(LocalDateTime.now());
		
		this.repository.save(entity);
		log.info("Usuário do id {} foi atualizado", entity.getId());
		
		return entity;
	}
	
	public void excluirTarefaPeloId(@NotNull @Valid UUID id) throws ResourceNotFoundException, AuthorizationException {
		Usuario usuarioEntity = this.buscarPeloId(id);
		
		if(usuarioEntity == null)
			throw new ResourceNotFoundException("Usuário pelo id %s não existe", id);
		
		if(!usuarioEntity.getNomeUsuario().equals(UsuarioUtils.getUsuarioLogado()))
			throw new AuthorizationException("Apenas o próprio usuário tem permissão para se excluir");
		
		this.repository.deleteById(id);
		log.info("Usuário pelo id {} foi excluído", id);
	}

}
