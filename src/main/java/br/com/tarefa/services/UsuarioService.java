package br.com.tarefa.services;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
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

/**
 * Serviço responsável pela gestão de usuários, incluindo operações como busca, criação,
 * atualização e exclusão. Este serviço utiliza um repositório para persistência de dados e
 * implementa caching para otimizar o acesso a informações de usuários.
 * 
 * @author Daniel Henrique Cavalcante da Silva
 */
@Slf4j
@Service
@Validated
public class UsuarioService {
	
	private final static String CACHE_USUARIOS = "usuarios";
	private final UsuarioRepository repository;
	private final UsuarioMapper mapper;
	private final CacheManager cacheManager;

	public UsuarioService(UsuarioRepository repository, UsuarioMapper mapper, CacheManager cacheManager) {
		this.repository = repository;
		this.mapper = mapper;
		this.cacheManager = cacheManager;
	}
	
	/**
	 * Busca um usuário pelo ID fornecido.
	 *
	 * Este método consulta o banco de dados para encontrar um usuário correspondente ao ID especificado.
	 * Se o usuário for encontrado, o objeto {@link Usuario} correspondente é retornado. Caso contrário,
	 * o método retornará null. O resultado da busca é armazenado em cache com a chave igual ao ID
	 * do usuário. Se o resultado for null, ele não será armazenado no cache.
	 *
	 * Além disso, o método verifica se o nome de usuário do usuário encontrado corresponde ao usuário
	 * atualmente logado. Se não corresponder, será proibido a visualização indicando que apenas o 
	 * usuário pode visualizar seus próprios dados.
	 *
	 * @param o ID do usuário a ser buscado, não pode ser null.
	 * @return o usuário correspondente ao ID fornecido, ou null se não for encontrado.
	 * @throws AuthorizationException se o usuário logado não for o mesmo que o usuário encontrado.
	 */
	@Cacheable(value = CACHE_USUARIOS, key = "#id", unless = "#result == null")
	public Usuario buscarPeloId(@NotNull @Valid UUID id) {
		Usuario usuario = this.repository.findById(id).orElse(null);
		
		if(usuario != null && !usuario.getNomeUsuario().equals(UsuarioUtils.getUsuarioLogado()))
			throw new AuthorizationException("Apenas o usuário tem permissão para visualizar seus dados");

		return usuario;
	}
	
	/**
	 * Busca um usuário pelo nome de usuário fornecido.
	 *
	 * Este método consulta o banco de dados para encontrar um usuário correspondente ao nome de
	 * usuário especificado. Se o usuário for encontrado, o objeto {@link Usuario} correspondente
	 * é retornado; caso contrário, o método retornará null. 
	 * O resultado da busca é armazenado em cache com a chave igual ao nome de usuário. 
	 * Se o resultado for null, ele não será armazenado no cache.
	 *
	 * @param nomeUsuario o nome de usuário a ser buscado.
	 * @return o usuário correspondente ao nome de usuário fornecido.
	 * 
	 * @Cacheable Anotação que indica que o resultado deste método deve ser armazenado em cache
	 *             sob a chave correspondente ao nomeUsuario, permitindo acesso rápido em
	 *             futuras solicitações para a mesmo usuário.
	 */
	@Cacheable(value = CACHE_USUARIOS, key = "#nomeUsuario", unless = "#result == null")
	public Usuario buscarPeloNomeUsuario(@NotNull @Valid String nomeUsuario) {
		return this.repository.findByNomeUsuario(nomeUsuario).orElse(null);
	}
	
	/**
	 * Cria um novo usuário com base nos dados fornecidos no DTO.
	 *
	 * Este método verifica se o nome de usuário já está em uso antes de criar um novo registro.
	 * Caso o nome de usuário já exista, não será permitida a operação.
	 * Além disso, o método criptografa a senha do usuário antes de inserir na base de dados.
	 *
	 * @param dto um objeto {@link CriarUsuarioDTO} contendo os dados necessários para criar um usuário.
	 * @return o usuário recém-criado, representado por um objeto {@link Usuario}.
	 * @throws BusinessException se o nome de usuário fornecido já estiver em uso.
	 */
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
	
	/**
	 * Atualiza as informações de um usuário com base nos dados fornecidos no DTO.
	 *
	 * Este método busca o usuário atualmente logado e verifica se o nome de usuário
	 * a ser atualizado já está em uso por outro usuário. Sendo proibido a atualização caso o nome de usuário
	 * já exista e pertença a um usuário diferente. 
	 *
	 * As informações do usuário, como nome, nome de usuário e senha, são atualizadas
	 * com base nos dados do objeto {@link AtualizarUsuarioDTO}. A senha é criptografada
	 * utilizando o {@link BCryptPasswordEncoder} antes de ser armazenada.
	 * 
	 * Se todas as verificações forem bem-sucedidas, os dados do usuário na base de dados serão atualizados
	 * e o cache correspondente será atualizado usando a key "id". 
	 * Caso o nome de usuário seja alterado, o cache existente sob a key "nomeUsuario" atual será invalidado, 
	 * e uma nova key "nomeUsuario" com o valor atualizado será inserida.
	 *
	 * @param dto O objeto de transferência de dados que contém as novas informações do usuário.
	 *            Este parâmetro não pode ser nulo e deve ser válido.
	 * @throws AuthorizationException Se o nome de usuário fornecido já estiver associado a outro usuário,
	 *                                ou se ocorrer uma tentativa de atualização não autorizada.
	 * @return O objeto {@link Usuario} atualizado.
	 */
	public Usuario atualizarUsuario(@NotNull @Valid AtualizarUsuarioDTO dto) throws AuthorizationException {
		Usuario entity = this.buscarPeloNomeUsuario(UsuarioUtils.getUsuarioLogado());
		
		if(!entity.getNomeUsuario().equals(dto.getNomeUsuario())) {
			Usuario outroUsuario = this.buscarPeloNomeUsuario(dto.getNomeUsuario());
			
			if(outroUsuario != null && !outroUsuario.getId().equals(entity.getId()))
				throw new AuthorizationException("O nome de usuário %s já está associado a outro usuário", dto.getNomeUsuario());
			
			this.cacheManager.getCache(CACHE_USUARIOS).evict(entity.getNomeUsuario());
		}
		
		entity.setNome(dto.getNome());
		entity.setNomeUsuario(dto.getNomeUsuario());
		entity.setSenha(new BCryptPasswordEncoder().encode(dto.getSenha()));
		entity.setDataAtualizacao(LocalDateTime.now());
		
		this.repository.save(entity);
		this.cacheManager.getCache(CACHE_USUARIOS).put(entity.getId(), entity);
		this.cacheManager.getCache(CACHE_USUARIOS).put(entity.getNomeUsuario(), entity);
	
		log.info("Usuário do id {} foi atualizado", entity.getId());
		
		return entity;
	}
	
	/**
	 * Exclui um usuário pelo seu identificador único (UUID).
	 *
	 * Este método verifica se o usuário existe. Somente o próprio usuário pode se excluir, 
	 * sendo proibido a exclusão de outro usuário que seja diferente do seu.
	 * Se todas as verificações forem bem-sucedidas, exclui o usuário da base de dados e
     * remove o usuário do cache.
	 *
	 * @param id O identificador único do usuário a ser excluído. Este parâmetro não pode ser nulo.
	 * @throws ResourceNotFoundException Se o usuário com o ID fornecido não existir.
	 * @throws AuthorizationException Se o usuário não tiver permissão para excluir.
	 */
	public void excluirUsuarioPeloId(@NotNull @Valid UUID id) throws ResourceNotFoundException, AuthorizationException {
		Usuario usuarioEntity = this.repository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário pelo id %s não existe", id));
		
		if(!usuarioEntity.getNomeUsuario().equals(UsuarioUtils.getUsuarioLogado()))
			throw new AuthorizationException("Apenas o próprio usuário tem permissão para se excluir");
		
		this.repository.deleteById(id);
		this.cacheManager.getCache(CACHE_USUARIOS).evict(id);
		this.cacheManager.getCache(CACHE_USUARIOS).evict(usuarioEntity.getNomeUsuario());
		
		log.info("Usuário pelo id {} foi excluído", id);
	}

}
