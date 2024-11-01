package br.com.tarefa.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import br.com.tarefa.dtos.AtualizarTarefaDTO;
import br.com.tarefa.dtos.CriarTarefaDTO;
import br.com.tarefa.dtos.FiltroTarefaDTO;
import br.com.tarefa.entities.Tarefa;
import br.com.tarefa.entities.Usuario;
import br.com.tarefa.entities.enums.StatusTarefa;
import br.com.tarefa.exceptions.AuthorizationException;
import br.com.tarefa.exceptions.BusinessException;
import br.com.tarefa.exceptions.ResourceNotFoundException;
import br.com.tarefa.mappers.TarefaMapper;
import br.com.tarefa.repositories.TarefaRepository;
import br.com.tarefa.utils.UsuarioUtils;

@Service
@Validated
public class TarefaService {

	private final TarefaRepository repository;
	private final UsuarioService usuarioService;
	private final TarefaMapper mapper;

	public TarefaService(TarefaRepository repository, UsuarioService usuarioService, TarefaMapper mapper) {
		this.repository = repository;
		this.usuarioService = usuarioService;
		this.mapper = mapper;
	}

	public Tarefa buscarPeloId(@NotNull @Valid UUID id) {
		return this.repository.findById(id).orElse(null);
	}
	
	public Tarefa buscarPeloTitulo(@NotBlank @Valid String titulo) {
		return this.repository.findByTitulo(titulo).orElse(null);
	}
	
	public List<Tarefa> listarTarefasComFiltro(FiltroTarefaDTO filtro) {
		return this.repository.findAllByFilter(filtro);
	}
	
	public List<Tarefa> listarTarefas() {
		return this.repository.findAll();
	}

	public Tarefa criarTarefa(@NotNull @Valid CriarTarefaDTO dto) {
		Tarefa tituloTarefa = this.buscarPeloTitulo(dto.getTitulo());
		
		if (tituloTarefa != null)
			throw new BusinessException("Título %s já está cadastrado em outra tarefa", dto.getTitulo());
		
		Usuario usuario = this.usuarioService.buscarPeloNomeUsuario(UsuarioUtils.getUsuarioLogado());

		Tarefa tarefaEntity = this.mapper.criarTarefaDTOToTarefa(dto);
		LocalDateTime dataAtual = LocalDateTime.now();
		tarefaEntity.setDataCriacao(dataAtual);
		tarefaEntity.setDataAtualizacao(dataAtual);
		tarefaEntity.setStatus(StatusTarefa.PENDENTE);
		tarefaEntity.setUsuario(usuario);
		
		this.repository.save(tarefaEntity);
		return tarefaEntity;
	}
	
	public Tarefa atualizarTarefa(@NotNull @Valid AtualizarTarefaDTO dto) {
		Tarefa tarefaEntity = this.buscarPeloId(dto.getId());
		
		if(tarefaEntity == null)
			throw new ResourceNotFoundException("Tarefa pelo id %s não existe", dto.getId());
		
		if(!tarefaEntity.getUsuario().getNomeUsuario().equals(UsuarioUtils.getUsuarioLogado()))
			throw new AuthorizationException("Apenas o usuário que criou a tarefa pode atualizá-la");
		
		tarefaEntity.setTitulo(dto.getTitulo());
		tarefaEntity.setDescricao(dto.getDescricao());
		tarefaEntity.setStatus(dto.getStatus());
		tarefaEntity.setDataAtualizacao(LocalDateTime.now());
		
		this.repository.save(tarefaEntity);
		return tarefaEntity;
	}
	
	public void excluirTarefaPeloId(@NotNull @Valid UUID id) {
		Tarefa tarefaEntity = this.buscarPeloId(id);
		
		if(tarefaEntity == null)
			throw new ResourceNotFoundException("Tarefa pelo id %s não existe", id);
		
		if(!tarefaEntity.getUsuario().getNomeUsuario().equals(UsuarioUtils.getUsuarioLogado()))
			throw new AuthorizationException("Apenas o usuário que criou a tarefa tem permissão para excluí-la");
		
		this.repository.deleteById(id);
	}

}
