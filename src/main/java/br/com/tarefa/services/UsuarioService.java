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
	
	public Usuario criarUsuario(@NotNull @Valid CriarUsuarioDTO dto) {
		Usuario outroUsuario = this.buscarPeloNomeUsuario(dto.getNomeUsuario());
		
		if(outroUsuario != null)
			throw new BusinessException("Nome de usuário %s já existente", dto.getNomeUsuario());
		
		LocalDateTime dataAtual = LocalDateTime.now();
		Usuario entity = this.mapper.criarUsuarioDTOToUsuario(dto);
		entity.setSenha(new BCryptPasswordEncoder().encode(entity.getSenha()));
		entity.setDataCriacao(dataAtual);
		entity.setDataAtualizacao(dataAtual);

		this.repository.save(entity);
		return entity;
	}
	
	public Usuario atualizarUsuario(@NotNull @Valid AtualizarUsuarioDTO dto) {
		Usuario entity = this.buscarPeloNomeUsuario(UsuarioUtils.getUsuarioLogado());
		
		if(!entity.getNomeUsuario().equals(dto.getNomeUsuario())) {
			Usuario outroUsuario = this.buscarPeloNomeUsuario(dto.getNomeUsuario());
			
			if(outroUsuario != null && !outroUsuario.getId().equals(entity.getId()))
				throw new BusinessException("O nome de usuário %s já está associado a outro usuário", dto.getNomeUsuario());
		}
		
		entity.setNome(dto.getNome());
		entity.setNomeUsuario(dto.getNomeUsuario());
		entity.setSenha(new BCryptPasswordEncoder().encode(dto.getSenha()));
		entity.setDataAtualizacao(LocalDateTime.now());
		
		this.repository.save(entity);
		return entity;
	}
	
	public void excluirTarefaPeloId(@NotNull @Valid UUID id) {
		Usuario usuarioEntity = this.buscarPeloId(id);
		
		if(usuarioEntity == null)
			throw new ResourceNotFoundException("Usuário pelo id %s não existe", id);
		
		if(!usuarioEntity.getNomeUsuario().equals(UsuarioUtils.getUsuarioLogado()))
			throw new AuthorizationException("Apenas o próprio usuário tem permissão para se excluir");
		
		this.repository.deleteById(id);
	}

}
