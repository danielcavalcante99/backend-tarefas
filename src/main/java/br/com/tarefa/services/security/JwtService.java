package br.com.tarefa.services.security;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import br.com.tarefa.dtos.security.AuthenticationRequestDTO;
import br.com.tarefa.dtos.security.AuthenticationTokenDTO;
import br.com.tarefa.exceptions.RevokeTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço responsável pela manipulação de tokens JWT (JSON Web Tokens).
 *
 * Esta classe oferece métodos para geração, revogação e validação de tokens de autenticação
 * e refresh tokens. Utiliza um cache para armazenar tokens revogados, garantindo que tokens
 * revogados não possam ser utilizados após o logout do usuário. 
 *
 * Os tokens são gerados com base em um segredo e têm tempos de expiração configuráveis.
 * 
 * @author Daniel Henrique Cavalcante da Silva
 */
@Slf4j
@Service
public class JwtService {

	private static final String CACHE_BLACKLISTED_TOKENS = "blacklistedTokens";
	@Value("${application.security.jwt.secret-key}")
	private String secretKey;
	@Value("${application.security.jwt.expiration}")
	private long jwtExpiration;
	@Value("${application.security.jwt.refresh-token.expiration}")
	private long refreshExpiration;
	
	private final CacheManager cacheManager;
	
	public JwtService(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
	/**
	 * Revoga o token JWT fornecido no cabeçalho de autorização.
	 *
	 * Este método extrai o token do cabeçalho de autorização passado como
	 * parâmetro e verifica se o token está expirado. Se o token já tiver
	 * expirado, uma exceção é lançada. 
	 * Caso contrário, o token é adicionado à lista negra (cache) para
	 * impedir seu uso no futuro.
	 *
	 * @param authHeaderAuthorization o cabeçalho de autorização contendo o token JWT,
	 *                                no formato "Bearer {token}". Se o cabeçalho for 
	 *                                nulo ou não estiver no formato esperado, o 
	 *                                token não será revogado.
	 * @throws RevokeTokenException se o token estiver expirado e não puder ser revogado.
	 */
	public void revokeToken(@NotBlank @Valid String authHeaderAuthorization) {
		String token = null;
		if (authHeaderAuthorization != null && authHeaderAuthorization.startsWith("Bearer ")) {
			token = authHeaderAuthorization.substring(7);

			if (isTokenExpired(token))
				throw new RevokeTokenException("Não foi possível revogar o token JWT durante o logout, pois ele já está expirado");
		}
		
		Cache cacheBlackListedTokens = this.cacheManager.getCache(CACHE_BLACKLISTED_TOKENS);
		cacheBlackListedTokens.putIfAbsent(token, token);
		log.info("Token revogado com sucesso");
	}
	
	/**
	 * Gera um par de tokens de autenticação (access token e refresh token) a partir das informações
	 * fornecidas no objeto {@link AuthenticationRequestDTO}.
	 *
	 * Este método cria um access token e um refresh token, ambos associados ao usuário autenticado.
	 * O access token é usado para autenticar solicitações subsequentes, enquanto o refresh token
	 * pode ser utilizado para obter um novo access token quando o anterior expirar.
	 *
	 * @param dto o objeto que contém as informações necessárias para gerar os tokens, incluindo
	 *            credenciais de autenticação do usuário.
	 * @return um objeto {@link AuthenticationTokenDTO} contendo o access token gerado, o tempo de
	 *         expiração do access token, o refresh token gerado e o tempo de expiração do refresh token.
	 */
	public AuthenticationTokenDTO generateToken(@NotNull @Valid AuthenticationRequestDTO dto) {	
		String acesstoken = generateToken(new HashMap<>(), dto);
		String refreshToken = generateRefreshToken(dto);
		
		log.info("Novo accesstoken e refreshToken gerados para o usuário: {}", dto.getUsername());
		
		return AuthenticationTokenDTO.builder()
				.accessToken(acesstoken)
				.expiresInMl(this.jwtExpiration)
				.refreshToken(refreshToken)
				.refreshExpiresInMl(this.refreshExpiration)
				.build();
	}
	
	public void validTokenRevoke(@NotBlank @Valid String token) throws RevokeTokenException {
		if(isTokenRevoke(token)) {
			throw new RevokeTokenException("O token JWT que está sendo utlizando foi revogado");
		}
	}
	
	public boolean isTokenValid(@NotBlank @Valid String token, @NotNull @Valid UserDetails userDetails) {
		final String username = this.extractUsername(token);
		return (username.equals(userDetails.getUsername())) && !this.isTokenExpired(token);
	}
	
	public String extractUsername(@NotBlank @Valid String token) {
		try {
			return extractClaim(token, Claims::getSubject);
		} catch (JwtException e) {
			return null;
		}
	}
	
	public boolean isTokenExpired(String token) {
		try {
			return this.extractExpiration(token).before(new Date());
		} catch (ExpiredJwtException e) {
			return true;
		}
	}
	
	public String generateToken(Map<String, Object> extraClaims, AuthenticationRequestDTO dto) {
		return buildToken(extraClaims, dto, jwtExpiration);
	}

	public String generateRefreshToken(AuthenticationRequestDTO dto) {
		return buildToken(new HashMap<>(), dto, refreshExpiration);
	}
	
	private boolean isTokenRevoke(String token) {
		Cache cacheBlackListedTokens = this.cacheManager.getCache(CACHE_BLACKLISTED_TOKENS);
		String keyTokenRevoke = cacheBlackListedTokens.get(token, String.class);
		return StringUtils.isNotBlank(keyTokenRevoke);
	}
	
	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}
	
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(this.getSignInKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	private Key getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	private String buildToken(Map<String, Object> extraClaims, AuthenticationRequestDTO dto, long expiration) {
		return Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(dto.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(this.getSignInKey(), SignatureAlgorithm.HS256)
				.compact();
	}
	
}