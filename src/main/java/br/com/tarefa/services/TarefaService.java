package br.com.tarefa.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço responsável pela manipulação das tarefas.
 * 
 * Esta classe fornece métodos para criar, atualizar, excluir e listar tarefas,
 * além de aplicar filtros na busca de tarefas. As operações de busca são
 * otimizadas através de caching, melhorando a performance das chamadas
 * subsequentes. 
 * 
 * @author Daniel Henrique Cavalcante da Silva
 */
@Slf4j
@Service
@Validated
public class TarefaService {

	private final static String CACHE_TAREFAS = "tarefas";
	private final static String CACHE_LISTAR_TAREFAS = "listarTarefas";
	private final TarefaRepository repository;
	private final UsuarioService usuarioService;
	private final TarefaMapper mapper;
	
	public TarefaService(TarefaRepository repository, UsuarioService usuarioService, TarefaMapper mapper) {
		this.repository = repository;
		this.usuarioService = usuarioService;
		this.mapper = mapper;
	}

	/**
	 * Recupera uma tarefa com base no ID fornecido.
	 * 
	 * Este método verifica se a tarefa existe no banco de dados e, se encontrada, a armazena em cache 
	 * para futuras consultas. Se a tarefa não existir, não será armazenada no cache.
	 * 
	 * @param id O UUID que representa o ID da tarefa a ser buscada.
	 * @return A tarefa correspondente ao ID fornecido, ou null se não houver tarefa com esse ID.
	 * 
	 * @Cacheable Anotação que indica que o resultado deste método deve ser armazenado em cache
	 *             sob a chave correspondente ao ID da tarefa, permitindo acesso rápido em
	 *             futuras solicitações para a mesma tarefa.
	 */
	@Cacheable(value = CACHE_TAREFAS, key = "#id", unless = "#result == null")
	public Tarefa buscarPeloId(@NotNull @Valid UUID id) {
		return this.repository.findById(id).orElse(null);
	}
	
	/**
	 * Recupera uma lista de todas as tarefas armazenadas.
	 * 
	 * Este método busca todas as tarefas no banco de dados e, se encontrada, armazena o resultado em cache.
	 * Isso melhora a performance em chamadas subsequentes, evitando consultas repetidas ao banco de dados.
	 * 
	 * @return Uma lista de todas as tarefas disponíveis.
	 * 
	 * @Cacheable Anotação que indica que o resultado deste método deve ser armazenado em cache
	 *             sob a chave padrão, facilitando acessos futuros às tarefas sem necessidade
	 *             de nova consulta no banco de dados.
	 */
	@Cacheable(value = CACHE_LISTAR_TAREFAS, unless = "#result.isEmpty()")
	public List<Tarefa> listarTarefas() {
		return this.repository.findAll();
	}
	
	/**
	 * Recupera uma lista de tarefas aplicando os filtros especificados no DTO.
	 * 
	 * Este método busca todas as tarefas conforme definidos no objeto {@link FiltroTarefaDTO}. 
	 * Somente em caso de existir tarefas, os resultados são armazenados em cache
	 * para melhorar a performance em chamadas subsequentes com os mesmos filtros.
	 * 
	 * @param filtro O objeto {@link FiltroTarefaDTO} que contém os critérios de filtragem
	 *               para a busca das tarefas.
	 * @return Uma lista de tarefas que correspondem aos filtros aplicados.
	 * 
	 * @Cacheable Anotação que indica que o resultado deste método deve ser armazenado em cache
	 *             sob a chave gerada a partir da representação em string do objeto filtro.
	 */
	@Cacheable(value = CACHE_LISTAR_TAREFAS, key="#filtro.toString()",  unless = "#result.isEmpty()")
	public List<Tarefa> listarTarefasComFiltro(FiltroTarefaDTO filtro) {
		return this.repository.findAllByFilter(filtro);
	}
	
	/**
	 * Cria uma nova tarefa com base nas informações fornecidas no DTO.
	 * 
	 * Este método verifica se já existe uma tarefa com o mesmo título. Se um título duplicado
	 * for encontrado, uma exceção de negócio será lançada. Caso contrário, uma nova tarefa
	 * será criada e persistida no banco de dados. O método define o status inicialmente
	 * da tarefa como PENDENTE e associa a tarefa ao usuário atualmente logado, logo após 
	 * todas as entradas do cache "listarTarefas" são removidas, garantindo que a próxima consulta 
	 * reflita as mudanças.
	 * 
	 * @param dto O objeto {@link CriarTarefaDTO} que contém os dados necessários para criar a nova tarefa.
	 * @throws BusinessException Se já existir uma tarefa com o mesmo título.
	 * @return A nova tarefa criada como um objeto {@link Tarefa}.
	 * 
	 * @CacheEvict Anotação que indica que todas as entradas do cache "listarTarefas" devem ser removidas
	 *              após a execução deste método, garantindo que as informações em cache estejam sempre atualizadas.
	 */
	@CacheEvict(value = CACHE_LISTAR_TAREFAS, allEntries = true)
	public Tarefa criarTarefa(@NotNull @Valid CriarTarefaDTO dto) throws BusinessException {
		Optional<Tarefa> optTituloOutraTarefa = this.repository.findByTitulo(dto.getTitulo());		
		if (optTituloOutraTarefa.isPresent())
			throw new BusinessException("Título %s já está cadastrado em outra tarefa", dto.getTitulo());
		
		Usuario usuario = this.usuarioService.buscarPeloNomeUsuario(UsuarioUtils.getUsuarioLogado());

		Tarefa tarefaEntity = this.mapper.criarTarefaDTOToTarefa(dto);
		LocalDateTime dataAtual = LocalDateTime.now();
		tarefaEntity.setDataCriacao(dataAtual);
		tarefaEntity.setDataAtualizacao(dataAtual);
		tarefaEntity.setStatus(StatusTarefa.PENDENTE);
		tarefaEntity.setUsuario(usuario);
		
		this.repository.save(tarefaEntity);
		log.info("Tarefa {} criada", tarefaEntity.getTitulo());
		
		return tarefaEntity;
	}
	
	/**
	 * Atualiza uma tarefa existente com base nas informações fornecidas no DTO.
	 * 
	 * Este método realiza a atualização de uma tarefa com base nas informações fornecidas no
	 * objeto {@link AtualizarTarefaDTO}. Antes de realizar a atualização, o método verifica se a
	 * tarefa existe, se o usuário que está tentando realizar a atualização é o mesmo que criou a tarefa
	 * e se o título da tarefa não conflita com outra tarefa existente de outro usuário.
	 *
	 * Ao atualizar a tarefa, todas as entradas do cache "listarTarefas" são removidas,
	 * garantindo que a próxima consulta reflita as mudanças. O cache para a tarefa específica também é
	 * atualizado com as novas informações.
	 * 
	 * @param dto O objeto {@link AtualizarTarefaDTO} que contém os dados necessários para atualizar a tarefa.
	 * @throws ResourceNotFoundException Se a tarefa com o ID fornecido não for encontrada.
	 * @throws AuthorizationException Se o usuário atual não tiver permissão para atualizar a tarefa.
	 * @throws BusinessException Se o título da tarefa já estiver cadastrado em outra tarefa de outro usuário.
	 * @return A tarefa atualizada como um objeto {@link Tarefa}.
	 * 
	 * @CacheEvict Anotação que indica que todas as entradas do cache "listaTarefas" devem ser removidas
	 *              após a execução deste método.
	 * @CachePut Anotação que indica que a tarefa atualizada deve ser armazenada no cache "tarefas" com
	 *            a chave correspondente ao ID da tarefa.
	 */
	@CacheEvict(value = CACHE_LISTAR_TAREFAS, allEntries = true)
	@CachePut(value = CACHE_TAREFAS, key = "#dto.id")
	public Tarefa atualizarTarefa(@NotNull @Valid AtualizarTarefaDTO dto) throws ResourceNotFoundException, AuthorizationException {
		Tarefa tarefaEntity = this.buscarPeloId(dto.getId());
		if(tarefaEntity == null)
			throw new ResourceNotFoundException("Tarefa pelo id %s não existe", dto.getId());
		
		if(!tarefaEntity.getUsuario().getNomeUsuario().equals(UsuarioUtils.getUsuarioLogado()))
			throw new AuthorizationException("Apenas o usuário que criou a tarefa pode atualizá-la");
		
		Tarefa tituloOutraTarefa = this.repository.findByTitulo(dto.getTitulo()).orElse(null);
		if (tituloOutraTarefa != null && !tarefaEntity.getId().equals(tituloOutraTarefa.getId()))
			throw new BusinessException("Título %s já está cadastrado em outra tarefa de outro usuário", dto.getTitulo());
		
		tarefaEntity.setTitulo(dto.getTitulo());
		tarefaEntity.setDescricao(dto.getDescricao());
		tarefaEntity.setStatus(dto.getStatus());
		tarefaEntity.setDataAtualizacao(LocalDateTime.now());
		
		this.repository.save(tarefaEntity);
		log.info("Tarefa do id {} foi atualizada", tarefaEntity.getId());
		
		return tarefaEntity;
	}
	
	/**
	 * Exclui uma tarefa com base no ID fornecido.
	 * 
	 * Este método verifica se a tarefa existe e se o usuário que está tentando
	 * excluí-la é o mesmo que a criou. Se a tarefa não existir, ou se o usuário
	 * não tiver permissão para excluí-la, exceções apropriadas serão lançadas.
	 * Após a exclusão, a tarefa conforme seu ID é removida do cache "tarefas"
	 * e também todas as entradas do cache "listarTarefas" são removidas,
	 * garantindo que a próxima consulta reflita as mudanças.
	 * 
	 * @param id O UUID que representa o ID da tarefa a ser excluída.
	 * @throws ResourceNotFoundException Se a tarefa com o ID fornecido não for encontrada.
	 * @throws AuthorizationException Se o usuário atual não tiver permissão para excluir a tarefa.
	 * 
  	 * @CacheEvict A primeira anotação indica que todas as entradas do cache "listaTarefas" devem ser removidas.
  	 * 			   A segunda indica que o cache "tarefas" correspondente ao ID será removido. tudo isso após a 
  	 * 			   execução deste método.
	 */
	@Caching(evict = { 
			@CacheEvict(value = CACHE_LISTAR_TAREFAS, allEntries = true), 
			@CacheEvict(value = CACHE_TAREFAS, key = "#id")
	})
	public void excluirTarefaPeloId(@NotNull @Valid UUID id) throws ResourceNotFoundException, AuthorizationException {
		Tarefa tarefaEntity = this.buscarPeloId(id);	
		if(tarefaEntity == null)
			throw new ResourceNotFoundException("Tarefa pelo id %s não existe", id);
		
		if(!tarefaEntity.getUsuario().getNomeUsuario().equals(UsuarioUtils.getUsuarioLogado()))
			throw new AuthorizationException("Apenas o usuário que criou a tarefa tem permissão para excluí-la");
		
		this.repository.deleteById(id); 
		log.info("Tarefa pelo id {} foi excluída", id);
	}

}
